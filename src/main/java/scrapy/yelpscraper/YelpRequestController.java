package scrapy.yelpscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.lang.String;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

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
        try {
            Document doc = Jsoup.connect(formattedURL).get();
            YelpImageScraper scraper = new YelpImageScraper(doc);
            return scraper.scrapeYelpImgLinks();
        }
        catch(IOException e) {
            return null;
        }
    }
}
