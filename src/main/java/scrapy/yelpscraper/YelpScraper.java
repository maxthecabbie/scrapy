package scrapy.yelpscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Callable;

import java.util.ArrayList;
import java.util.HashMap;

public class YelpScraper implements Callable<YelpRequestResult> {
    private String yelpURL;
    private boolean initialRequest;
    private static final String IMG_SRC_SELECTOR = "div.media-landing_gallery ul li img[src]";
    private static final String ROOT_NAVBAR_SELECTOR = "div.media-header_root-navbar";
    private static final String TAB_LINK_SELECTOR = "a.tab-link.js-tab-link";
    private static final String TAB_LINK_COUNT_SELECTOR = "span.tab-link_count";
    private static final int JSOUP_CONNECT_TIMEOUT = 5000;

    public YelpScraper(String yelpURL) {
        this.yelpURL = yelpURL;
        this.initialRequest = false;
    }

    public YelpScraper(String yelpURL, boolean initialRequest) {
        this.yelpURL = yelpURL;
        this.initialRequest = initialRequest;
    }

    public YelpRequestResult scrapeYelp() {
        YelpRequestResult requestResult = new YelpRequestResult();

        try {
            Document doc = Jsoup.connect(yelpURL)
                    .timeout(JSOUP_CONNECT_TIMEOUT)
                    .get();

            if (initialRequest) {
                HashMap<String, Integer> imageGalleryData = getImageGalleryData(doc);
                requestResult.setImageGalleryData(imageGalleryData);
            }

            ArrayList<String> imgLinks = getImgLinks(doc);
            requestResult.setImgLinks(imgLinks);
        } catch (Exception e) {
        }

        return requestResult;
    }

    private ArrayList<String> getImgLinks(Document doc) {
        Elements imgSrcAttributes = doc.select(IMG_SRC_SELECTOR);
        ArrayList<String> imgLinks = new ArrayList<>();
        for (Element src : imgSrcAttributes) {
            imgLinks.add(src.attr("abs:src"));
        }
        return imgLinks;
    }

    private HashMap<String, Integer> getImageGalleryData(Document doc) {
        HashMap<String, Integer> imagesDataResult = new HashMap<>();
        int numAllImages = 0;
        int numFoodImages = 0;
        Elements rootNavbar = doc.select(ROOT_NAVBAR_SELECTOR);
        Elements tabItems = new Elements();

        for (Element rootNavEle : rootNavbar) {
            Elements tabLinkEle = rootNavEle.select(TAB_LINK_SELECTOR);
            if (tabLinkEle.size() > 0) {
                tabItems = tabLinkEle;
                break;
            }
        }

        for (Element tabItem : tabItems) {
            if (tabItem.select("span:contains(All)").size() > 0) {
                String allTitleAttr = tabItem.select(TAB_LINK_COUNT_SELECTOR).text();
                numAllImages = Integer.parseInt(allTitleAttr.replaceAll("[()]", ""));
            }
            else if (tabItem.select("span:contains(Food)").size() > 0) {
                String foodTitleAttr = tabItem.select(TAB_LINK_COUNT_SELECTOR).text();
                numFoodImages = Integer.parseInt(foodTitleAttr.replaceAll("[()]", ""));
            }
        }

        imagesDataResult.put("numAllImages", numAllImages);
        imagesDataResult.put("numFoodImages", numFoodImages);
        return imagesDataResult;
    }

    @Override
    public YelpRequestResult call() {
        return scrapeYelp();
    }
}
