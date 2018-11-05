package scrapy.yelpscraper;

import scrapy.utils.RequestBodyData;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.String;

public class YelpRequestController {
    private String yelpURL;
    private Integer picLimit;

    private final int NUM_IMAGES_IN_GALLERY = 30;
    private final String REQUEST_PROTOCOL = "https";
    private final String YELP_HOST = "www.yelp.com";
    private final int EXPECTED_PATH_LENGTH = 2;
    private final int THREAD_COUNT = 10;

    public YelpRequestController(RequestBodyData reqData) {
        this.yelpURL = reqData.getUrl();
        this.picLimit = reqData.getPicLimit();
    }

    private String formatURL(String url, YelpRequestResult yelpResult) {
        try {
            URL yelpPageURL = new URL(url);
            String[] path = yelpPageURL.getPath().replaceFirst("^/", "").split("/");
            if (path.length != EXPECTED_PATH_LENGTH) {
                throw new MalformedURLException("Unexpected path length");
            }
            String venueID = path[1];
            String formattedPath = "/biz_photos/" + venueID;
            return REQUEST_PROTOCOL + "://" + YELP_HOST + formattedPath + "?tab=food";
        }
        catch (Exception e) {
            String error = "Input URL error: " + e.getClass().getCanonicalName() +
                    " - " + e.getMessage();
            yelpResult.addError(error);
            return null;
        }
    }

    private int calculateNumRequests(HashMap<String, Integer> imageGalleryData) {
        int numAllImages = imageGalleryData.getOrDefault("numAllImages", 0);
        int numFoodImages = imageGalleryData.getOrDefault("numFoodImages", 0);
        int numRemainingImages = Math.min(numFoodImages - NUM_IMAGES_IN_GALLERY, picLimit - NUM_IMAGES_IN_GALLERY);
        if (numAllImages <= 0 || numFoodImages <= 0 || numRemainingImages <= 0) {
            return 0;
        }
        int numRequests = (int) Math.ceil((double) numRemainingImages/NUM_IMAGES_IN_GALLERY);
        return numRequests;
    }

    private String addGalleryStartParam(int requestNumber, String formattedURL) {
        int startNumber = requestNumber * NUM_IMAGES_IN_GALLERY;
        return formattedURL + "&start=" + Integer.toString(startNumber);
    }

    public YelpRequestResult makeYelpRequest() {
        YelpRequestResult yelpResult = new YelpRequestResult();
        String formattedURL = formatURL(yelpURL, yelpResult);

        if (formattedURL != null) {
            YelpScraper initialScraper = new YelpScraper(formattedURL, yelpResult);
            ArrayList<String> initialScrape = initialScraper.scrapeYelp(true);
            yelpResult.addImgLinks(initialScrape);
            int numRequests = calculateNumRequests(yelpResult.getImageGalleryData());

            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            ArrayList<Future<ArrayList<String>>> threadList = new ArrayList<>();

            for (int i = 0; i < numRequests; i++) {
                String formattedURLWithStartParam = addGalleryStartParam(i + 1, formattedURL);
                Callable<ArrayList<String>> callable = new YelpScraper(formattedURLWithStartParam, yelpResult);
                Future<ArrayList<String>> yelpScrapeFuture = executor.submit(callable);
                threadList.add(yelpScrapeFuture);
            }

            int i = 0;
            for (Future<ArrayList<String>> future : threadList) {
                try {
                    ArrayList<String> futureResult = future.get();
                    yelpResult.addImgLinks(futureResult);
                } catch (InterruptedException | ExecutionException e) {
                    String error = "Thread " + i + ": " + e.getClass().getCanonicalName() +
                            " - " + e.getMessage();
                    yelpResult.addError(error);
                } finally {
                    i++;
                }
            }

            try {
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                String error = "Executor shutdown: " + e.getClass().getCanonicalName() +
                        " - " + e.getMessage();
                yelpResult.addError(error);
            }
        }

        return yelpResult;
    }
}
