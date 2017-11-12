package scrapy.yelpscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class YelpImageScraper implements Callable<HashMap> {
    private String yelpURL;

    public YelpImageScraper(String yelpURL) {
        this.yelpURL = yelpURL;
    }

    public ArrayList<String> scrapeYelpImgLinks() {
        try {
            Document doc = Jsoup.connect(yelpURL).get();
            Elements imgSrcAttributes = doc.select("div.media-landing_gallery ul li img[src]");
            ArrayList<String> imgLinks = new ArrayList<>();
            for (Element src : imgSrcAttributes) {
                imgLinks.add(src.attr("abs:src"));
            }
            return imgLinks;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public HashMap<String, ArrayList<String>> call() {
        try {
            ArrayList<String> ImgLinks = scrapeYelpImgLinks();
            HashMap<String, ArrayList<String>> resultsHashMap = new HashMap<>();
            resultsHashMap.put("yelpImgLinks", ImgLinks);
            return resultsHashMap;
        } catch (Exception e){
            return null;
        }

    }
}
