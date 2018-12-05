package scrapy.yelpscraper;

import java.util.ArrayList;
import java.util.HashMap;

public class ScrapeResult {
    private int startNum;
    private ArrayList<String> imgLinks;
    private ArrayList<String> errors;
    private HashMap<String, Integer> imgGalleryData;

    public ScrapeResult(int startNum, ArrayList<String> imgLinks, ArrayList<String> errors) {
        this.startNum = startNum;
        this.imgLinks = imgLinks;
        this.errors = errors;
        this.imgGalleryData = new HashMap<>();
    }

    public int getStartNum() {
        return startNum;
    }

    public ArrayList<String> getImgLinks() {
        return imgLinks;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public HashMap<String, Integer> getImgGalleryData() {
        return imgGalleryData;
    }

    public void setImgGalleryData(HashMap<String, Integer> imgGalleryData) {
        this.imgGalleryData = imgGalleryData;
    }
}
