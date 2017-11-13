package scrapy.yelpscraper;

import java.util.ArrayList;
import java.util.HashMap;

public class YelpRequestResult {
    private HashMap<String, Integer> imageGalleryData;
    private ArrayList<String> imgLinks;

    public HashMap<String, Integer> getImageGalleryData() {
        return imageGalleryData;
    }

    public ArrayList<String> getImgLinks() {
        return imgLinks;
    }

    public void setImageGalleryData(HashMap<String, Integer> imageGalleryData) {
         this.imageGalleryData = imageGalleryData;
    }

    public void setImgLinks(ArrayList<String> imgLinks) {
        this.imgLinks = imgLinks;
    }
}
