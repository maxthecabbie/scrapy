package scrapy.yelpscraper;

import java.util.ArrayList;
import java.lang.String;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.*;

public class YelpRequestController {
    private String yelpURL;

    public YelpRequestController(String yelpURL) {
        this.yelpURL = yelpURL;
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

    public ArrayList<String> makeYelpRequest() {
        String formattedURL = formatURL(yelpURL);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        ArrayList<Future<YelpRequestResult>> threadList = new ArrayList<>();
        Callable<YelpRequestResult> callable = new YelpScraper(formattedURL);
        YelpRequestResult requestResult = new YelpRequestResult();
        ArrayList<String> yelpImgLinks = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
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
