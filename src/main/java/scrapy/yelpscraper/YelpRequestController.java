package scrapy.yelpscraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

@Service
public class YelpRequestController {
    private static final int INITIAL_START_NUM = 0;
    private static final int NUM_IMGS_PER_PAGE = 30;
    private static final String REQUEST_PROTOCOL = "https";
    private static final String YELP_HOST = "www.yelp.com";
    private static final int EXPECTED_PATH_LENGTH = 2;

    private ExecutorCompletionService ecs;
    private YelpResult yelpResult;
    private String yelpUrl;
    private Integer picLimit;

    @Autowired
    private Scraper scraper;

    public YelpRequestController(ExecutorCompletionService ecs, YelpResult yelpResult) {
        this.ecs = ecs;
        this.yelpResult = yelpResult;
    }

    public void setYelpUrl(String yelpUrl) {
        this.yelpUrl = yelpUrl;
    }

    public void setPicLimit(int picLimit) {
        this.picLimit = picLimit;
    }

    private String formatUrl(String yelpUrl) {
        try {
            URL yelpPageUrl = new URL(yelpUrl);
            String protocol = yelpPageUrl.getProtocol();
            String host = yelpPageUrl.getHost();
            String[] path = yelpPageUrl.getPath().replaceFirst("^/", "").split("/");

            if (!protocol.equals(REQUEST_PROTOCOL) || !host.equals(YELP_HOST) || path.length != EXPECTED_PATH_LENGTH) {
                throw new MalformedURLException("Invalid URL provided");
            }

            String venueID = path[1];
            String formattedPath = "/biz_photos/" + venueID;
            return protocol + "://" + host + formattedPath + "?tab=food";
        }
        catch (Exception e) {
            String error = "Input Url error: " +
                    e.getClass().getCanonicalName() + " - " + e.getMessage();
            yelpResult.addError(error);
        }
        return null;
    }

    private int calculateAdditionalRequests(HashMap<String, Integer> imgGalleryData) {
        int numAllImgs = imgGalleryData.getOrDefault("numAllImgs", 0);
        int numFoodImgs = imgGalleryData.getOrDefault("numFoodImgs", 0);
        int numRemainingImgs = Math.min(numFoodImgs - NUM_IMGS_PER_PAGE, picLimit - NUM_IMGS_PER_PAGE);
        if (numAllImgs <= 0 || numFoodImgs <= 0 || numRemainingImgs <= 0) {
            return 0;
        }
        int numRequests = (int) Math.ceil((double) numRemainingImgs/ NUM_IMGS_PER_PAGE);
        return numRequests;
    }

    private ConcurrentLinkedQueue<String> genYelpUrls(String formattedUrl, int numRequests) {
        ConcurrentLinkedQueue<String> yelpUrls = new ConcurrentLinkedQueue<>();
        int initialStartNum = 1;
        for (int i = 0; i < numRequests; i++) {
            int startNum = (initialStartNum + i) * 30;
            yelpUrls.add(formattedUrl + "&start=" + startNum);
        }
        return yelpUrls;
    }

    public HashMap<String, ArrayList<String>> fetchImgLinks() {
        scraper.setYelpResult(yelpResult);
        String formattedUrl = formatUrl(yelpUrl);

        if (formattedUrl != null) {
            PaginatedScrapeResult initialScrape = scraper.scrapeYelp(INITIAL_START_NUM, formattedUrl);
            yelpResult.addPageScrapeResult(initialScrape);
            int additionalRequests = calculateAdditionalRequests(yelpResult.getImgGalleryData());
            scraper.setTaskList(genYelpUrls(formattedUrl, additionalRequests));

            for (int i = 0; i < additionalRequests; i++) {
                Callable<Scraper> task = scraper;
                ecs.submit(task);
            }

            try {
                for (int j = 0; j < additionalRequests; j++) {
                    PaginatedScrapeResult pageScrapeRes = (PaginatedScrapeResult) ecs.take().get();
                    if (pageScrapeRes != null) {
                        yelpResult.addPageScrapeResult(pageScrapeRes);
                    } else {
                        String error = "Thread error: A thread failed to fetch images " +
                                "of a page in the photo gallery for the restaurant";
                        yelpResult.addError(error);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                String error = "Thread error: " + e.getClass().getCanonicalName() + " - " + e.getMessage();
                yelpResult.addError(error);
            }
        }

        return yelpResult.prepareResult();
    }
}
