package scrapy.yelpscraper;

import java.util.ArrayList;
import java.util.HashMap;

public class YelpRequestResult {
    private HashMap<String, Integer> imageGalleryData;
    private ArrayList<String> imgLinks;

    public YelpRequestResult() {
        imageGalleryData = new HashMap<>();
        imgLinks = new ArrayList<>(); }

    public HashMap<String, Integer> getImageGalleryData() {
        return imageGalleryData;
    }

    public ArrayList<String> getImgLinks() {
        return imgLinks;
    }

    public void setImageGalleryData(HashMap<String, Integer> data) {
        for (String key : data.keySet()) {
            imageGalleryData.put(key, data.get(key));
        }
    }

    public void setImgLinks(ArrayList<String> imgLinks) {
        this.imgLinks = imgLinks;
    }
}
