package scrapy.yelpscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class YelpScraper implements Callable<YelpRequestResult> {
    private String yelpURL;
    private static final String IMG_SRC_SELECTOR = "div.media-landing_gallery ul li img[src]";
    private static final String ROOT_NAVBAR_SELECTOR = "div.media-header_root-navbar";
    private static final String TAB_LINK_SELECTOR = "a.tab-link.js-tab-link";
    private static final String TAB_LINK_COUNT_SELECTOR = "span.tab-link_count";

    public YelpScraper(String yelpURL) {
        this.yelpURL = yelpURL;
    }

    private YelpRequestResult scrapeYelp() {
        try {
            YelpRequestResult requestResult = new YelpRequestResult();
            Document doc = Jsoup.connect(yelpURL).get();
            HashMap<String, Integer> imageGalleryData = getImagesData(doc);
            Elements imgSrcAttributes = doc.select(IMG_SRC_SELECTOR);
            ArrayList<String> imgLinks = new ArrayList<>();
            for (Element src : imgSrcAttributes) {
                imgLinks.add(src.attr("abs:src"));
            }
            requestResult.setImgLinks(imgLinks);
            requestResult.setImageGalleryData(imageGalleryData);
            return requestResult;
        } catch (IOException e) {
            return null;
        }
    }

    private HashMap<String, Integer> getImagesData(Document doc) {
        HashMap<String, Integer> imagesDataResult = new HashMap<>();
        int numAllImages = 0;
        int numFoodImages = 0;
        Element rootNavbar = doc.select(ROOT_NAVBAR_SELECTOR).get(0);
        Elements tabItems = rootNavbar.select(TAB_LINK_SELECTOR);

        for (Element tabItem : tabItems) {
            if (tabItem.select("span[title=All]").size() > 0) {
                String allTitleAttr = tabItem.select(TAB_LINK_COUNT_SELECTOR).attr("title");
                numAllImages = Integer.parseInt(allTitleAttr.replaceAll("[()]", ""));
            }
            else if (tabItem.select("span[title=Food]").size() > 0) {
                String foodTitleAttr = tabItem.select(TAB_LINK_COUNT_SELECTOR).attr("title");
                numFoodImages = Integer.parseInt(foodTitleAttr.replaceAll("[()]", ""));
            }
        }
        imagesDataResult.put("numAllImages", numAllImages);
        imagesDataResult.put("numFoodImages", numFoodImages);
        return imagesDataResult;
    }

    @Override
    public YelpRequestResult call() {
        try {
            YelpRequestResult yelpScrapeResult = scrapeYelp();
            return yelpScrapeResult;
        } catch (Exception e){
            return null;
        }

    }
}
