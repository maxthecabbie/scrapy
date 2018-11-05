package scrapy.yelpscraper;

import java.util.ArrayList;
import java.util.HashMap;

public class YelpRequestResult {
    private HashMap<String, Integer> imageGalleryData;
    private ArrayList<String> imgLinks;
    private ArrayList<String> errors;

    public YelpRequestResult() {
        imageGalleryData = new HashMap<>();
        imgLinks = new ArrayList<>();
        errors = new ArrayList<>();
    }

    public HashMap<String, Integer> getImageGalleryData() {
        return imageGalleryData;
    }

    public ArrayList<String> getImgLinks() {
        return imgLinks;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void setImageGalleryData(HashMap<String, Integer> data) {
        for (String key : data.keySet()) {
            imageGalleryData.put(key, data.get(key));
        }
    }

    public void setImgLinks(ArrayList<String> imgLinks) {
        this.imgLinks = imgLinks;
    }

    public void addImgLinks(ArrayList<String> imgLinks) {
        this.imgLinks.addAll(imgLinks);
    }

    public void addError(String error) {
        errors.add(error);
    }
}
