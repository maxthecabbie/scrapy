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

    private String formatURL(String url) {
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

    public ArrayList<String> makeYelpRequest() {
        String formattedURL = formatURL(yelpURL);
        ArrayList<String> yelpImgLinks = new ArrayList<>();

        if (formattedURL != null) {
            YelpScraper initialYelpReq = new YelpScraper(formattedURL, true);
            YelpRequestResult initialYelpReqResult = initialYelpReq.scrapeYelp();
            yelpImgLinks.addAll(initialYelpReqResult.getImgLinks());
            int numRequests = calculateNumRequests(initialYelpReqResult.getImageGalleryData());

            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            ArrayList<Future<YelpRequestResult>> threadList = new ArrayList<>();

            for (int i = 0; i < numRequests; i++) {
                String formattedURLWithStartParam = addGalleryStartParam(i + 1, formattedURL);
                Callable<YelpRequestResult> callable = new YelpScraper(formattedURLWithStartParam, false);
                Future<YelpRequestResult> yelpReqFuture = executor.submit(callable);
                threadList.add(yelpReqFuture);
            }

            for (Future<YelpRequestResult> future : threadList) {
                try {
                    YelpRequestResult futureResult = future.get();
                    yelpImgLinks.addAll(futureResult.getImgLinks());
                } catch (InterruptedException | ExecutionException e) {
                }
            }

            try {
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
            }
        }

        return yelpImgLinks;
    }
}
