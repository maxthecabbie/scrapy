package scrapy.yelpscraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class YelpImageScraper {
    private Document doc;

    public YelpImageScraper(Document doc) {
        this.doc = doc;
    }

    public ArrayList<String> scrapeYelpImgLinks() {
        Elements imgSrcAttributes = doc.select("div.media-landing_gallery ul li img[src]");
        ArrayList<String> yelpImgLinks = new ArrayList<>();
        for (Element src : imgSrcAttributes) {
            yelpImgLinks.add(src.attr("abs:src"));
        }
        return yelpImgLinks;
    }
}
