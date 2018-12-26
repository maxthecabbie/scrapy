package scrapy.yelpscraper;

import scrapy.utils.RequestBodyData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

@Service
public class YelpScraperController {
    private static final String INITIAL_REQ = "initialRequest";
    private static final String ADDITIONAL_REQ = "additionalRequest";
    private static final int INITIAL_START_NUM = 0;
    private static final int ADDITIONAL_START_NUM = 30;
    private static final int NUM_IMGS_PER_PAGE = 30;
    private static final String REQUEST_PROTOCOL = "https";
    private static final String YELP_HOST = "www.yelp.com";
    private static final int EXPECTED_PATH_LENGTH = 2;

    private ExecutorCompletionService ecs;
    private RequestBodyData reqData;

    @Autowired
    private PageScraper pageScraper;

    public YelpScraperController(ExecutorCompletionService ecs) {
        this.ecs = ecs;
    }

    public void setReqData(RequestBodyData reqData) {
        this.reqData = reqData;
    }

    private String formatUrl(String yelpUrl, YelpResult yelpResult) {
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

    private int calculateAdditionalRequests(int numFoodImgs) {
        int numRemainingImgs = Math.min(numFoodImgs, reqData.getPicLimit());
        if (numFoodImgs <= 0 || numRemainingImgs <= 0) {
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

    public String fetchImgLinks(YelpResult yelpResult) {
        String formattedUrl = formatUrl(reqData.getYelpUrl(), yelpResult);

        if (formattedUrl != null) {
            if (reqData.getType().equals(INITIAL_REQ)) {
                return initialImgFetch(formattedUrl, yelpResult);
            } else if (reqData.getType().equals(ADDITIONAL_REQ)) {
                return additionalImgFetch(formattedUrl, yelpResult);
            }
        }
        return null;
    }

    private String initialImgFetch(String yelpUrl, YelpResult yelpResult) {
        PaginatedScrapeResult initialScrape = pageScraper.scrapeYelp(INITIAL_START_NUM, yelpUrl);
        yelpResult.addPageScrapeResult(initialScrape);
        return yelpResult.prepareJsonResult();
    }

    private String additionalImgFetch(String yelpUrl, YelpResult yelpResult) {
        PaginatedScrapeResult firstScrape = pageScraper.scrapeYelp(ADDITIONAL_START_NUM, yelpUrl);
        yelpResult.addPageScrapeResult(firstScrape);
        int additionalRequests = calculateAdditionalRequests(firstScrape.getNumFoodImgs());
        pageScraper.setTaskList(genYelpUrls(yelpUrl, additionalRequests));

        for (int i = 0; i < additionalRequests; i++) {
            Callable<PageScraper> task = pageScraper;
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

        return yelpResult.prepareJsonResult();
    }
}
