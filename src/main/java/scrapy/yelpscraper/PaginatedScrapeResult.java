package scrapy.yelpscraper;

import java.util.ArrayList;

public class PaginatedScrapeResult {
    private int startNum;
    private ArrayList<String> imgLinks;
    private ArrayList<String> errors;
    private Integer numFoodImgs;

    public PaginatedScrapeResult(int startNum, ArrayList<String> imgLinks, ArrayList<String> errors) {
        this.startNum = startNum;
        this.imgLinks = imgLinks;
        this.errors = errors;
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

    public int getNumFoodImgs() {
        return numFoodImgs;
    }

    public void setNumFoodImgs(int numFoodImgs) {
        this.numFoodImgs = numFoodImgs;
    }
}
