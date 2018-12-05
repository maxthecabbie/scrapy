package scrapy.yelpscraper;

import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
public class Scraper implements Callable {
    private static final int INITIAL_START_NUM = 0;
    private static final int JSOUP_CONNECT_TIMEOUT = 5000;
    private static final String IMG_SRC_SELECTOR = "div.media-landing_gallery ul li img[src]";
    private static final String ROOT_NAVBAR_SELECTOR = "div.media-header_root-navbar";
    private static final String TAB_LINK_SELECTOR = "a.tab-link.js-tab-link";
    private static final String TAB_LINK_COUNT_SELECTOR = "span.tab-link_count";

    private ConcurrentLinkedQueue<String> taskList;
    private YelpResult yelpResult;

    public void setTaskList(ConcurrentLinkedQueue<String> taskList) {
        this.taskList = taskList;
    }

    public void setYelpResult(YelpResult yelpResult) {
        this.yelpResult = yelpResult;
    }

    public ScrapeResult scrapeYelp(int startNum, String yelpUrl) {
        Document doc = null;
        ArrayList<String> linksFromScrape = new ArrayList<>();
        ArrayList<String> errors = new ArrayList<>();

        try {
            doc = Jsoup.connect(yelpUrl)
                    .timeout(JSOUP_CONNECT_TIMEOUT)
                    .get();

            linksFromScrape = scrapeImgLinks(doc);
        } catch (Exception e) {
            String error = "Error in request for Yelp URL with start param " + startNum +
                    ": " +  e.getClass().getCanonicalName() + " - " + e.getMessage();
            errors.add(error);
        }

        ScrapeResult result = new ScrapeResult(startNum, linksFromScrape, errors);
        if (startNum == INITIAL_START_NUM && doc != null) {
            result.setImgGalleryData(getImgGalleryData(doc));
        }
        return result;
    }

    private int getStartNum(String s) {
        int startNum = 0;
        int mult = 1;
        for (int i = s.length() - 1; i >= 0 && Character.isDigit(s.charAt(i)); i--) {
            startNum += mult * Character.getNumericValue(s.charAt(i));
            mult*= 10;
        }
        return startNum;
    }

    private ArrayList<String> scrapeImgLinks(Document doc) {
        Elements imgSrcAttributes = doc.select(IMG_SRC_SELECTOR);
        ArrayList<String> imgLinks = new ArrayList<>();

        for (Element src : imgSrcAttributes) {
            imgLinks.add(src.attr("abs:src"));
        }
        return imgLinks;
    }

    private HashMap<String, Integer> getImgGalleryData(Document doc) {
        HashMap<String, Integer> imagesDataResult = new HashMap<>();
        int numAllImgs = 0, numFoodImgs = 0;
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
                numAllImgs = Integer.parseInt(allTitleAttr.replaceAll("[()]", ""));
            }
            else if (tabItem.select("span:contains(Food)").size() > 0) {
                String foodTitleAttr = tabItem.select(TAB_LINK_COUNT_SELECTOR).text();
                numFoodImgs = Integer.parseInt(foodTitleAttr.replaceAll("[()]", ""));
            }
        }

        imagesDataResult.put("numAllImgs", numAllImgs);
        imagesDataResult.put("numFoodImgs", numFoodImgs);
        return imagesDataResult;
    }

    @Override
    public ScrapeResult call() {
        if (taskList.size() > 0) {
            String yelpUrl = taskList.remove();
            int startNum = getStartNum(yelpUrl);
            return scrapeYelp(startNum, yelpUrl);
        }
        return null;
    }
}
