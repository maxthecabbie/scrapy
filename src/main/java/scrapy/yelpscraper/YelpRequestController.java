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
    private static final int NUM_IMAGES_IN_GALLERY = 30;

    public YelpRequestController(RequestBodyData reqData) {
        this.yelpURL = reqData.getUrl();
        this.picLimit = reqData.getPicLimit();
    }

    private String formatURL(String url) {
        try {
            URL yelpPageURL = new URL(url);
            String protocol = yelpPageURL.getProtocol();
            String host = yelpPageURL.getHost();
            String path = yelpPageURL.getPath();
            path = formatURLPath(path);
            String formattedURL = protocol + "://" + host + path + "?tab=food";
            return formattedURL;
        }
        catch (MalformedURLException e) {
            return "";
        }
    }

    private String formatURLPath(String path) {
        String[] pathParts = path.replaceFirst("^/", "").split("/");
        String formattedPath = "";
        for (int i = 0; i < pathParts.length; i++) {
            if (pathParts[i].equals("biz") && i == 0) {
                formattedPath += "/biz_photos";
            }
            else {
                formattedPath += "/" + pathParts[i];
            }
        }
        return formattedPath;
    }

    private int calculateNumRequests(HashMap<String, Integer> imageGalleryData) {
        int numAllImages = imageGalleryData.get("numAllImages");
        int numFoodImages = imageGalleryData.get("numFoodImages");
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

        YelpScraper initialYelpReq = new YelpScraper(formattedURL, true);
        YelpRequestResult initialYelpReqResult = initialYelpReq.scrapeYelp();
        yelpImgLinks.addAll(initialYelpReqResult.getImgLinks());
        int numRequests = calculateNumRequests(initialYelpReqResult.getImageGalleryData());

        ExecutorService executor = Executors.newFixedThreadPool(10);
        ArrayList<Future<YelpRequestResult>> threadList = new ArrayList<>();

        for (int i = 0; i < numRequests; i++) {
            String formattedURLWithStartParam = addGalleryStartParam(i + 1, formattedURL);
            Callable<YelpRequestResult> callable = new YelpScraper(formattedURLWithStartParam);
            Future<YelpRequestResult> yelpReqFuture = executor.submit(callable);
            threadList.add(yelpReqFuture);
        }

        for (Future<YelpRequestResult> future : threadList) {
            try {
                YelpRequestResult futureResult = future.get();
                ArrayList<String> imgLinksResultList = futureResult.getImgLinks();
                yelpImgLinks.addAll(imgLinksResultList);
            } catch (InterruptedException | ExecutionException e) {
            }
        }

        try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        return yelpImgLinks;
    }
}
