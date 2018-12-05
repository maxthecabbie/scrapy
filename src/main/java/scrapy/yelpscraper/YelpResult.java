package scrapy.yelpscraper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class YelpResult {
    private HashMap<String, Integer> imgGalleryData;
    private TreeMap<Integer, ArrayList<String>> imgLinks;
    private ArrayList<String> errors;

    public YelpResult() {
        imgGalleryData = new HashMap<>();
        imgLinks = new TreeMap<>();
        errors = new ArrayList<>();
    }

    public HashMap<String, Integer> getImgGalleryData() {
        return imgGalleryData;
    }

    public ArrayList<String> getImgLinks() {
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<String>> entry : imgLinks.entrySet()) {
            result.addAll(entry.getValue());
        }
        return result;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void setImgGalleryData(HashMap<String, Integer> data) {
        for (String key : data.keySet()) {
            imgGalleryData.put(key, data.get(key));
        }
    }

    public void addImgLinks(int key, ArrayList<String> linksFromScrape) {
        this.imgLinks.put(key, linksFromScrape);
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void addError(ArrayList<String> errorList) {
        errors.addAll(errorList);
    }

    public void addScrapeResult(ScrapeResult scrapeRes) {
        int startNum = scrapeRes.getStartNum();
        ArrayList<String> imgLinks = scrapeRes.getImgLinks();
        ArrayList<String> errors = scrapeRes.getErrors();
        HashMap<String, Integer> imgGalleryData = scrapeRes.getImgGalleryData();

        addImgLinks(startNum, imgLinks);
        if (errors.size() > 0) {
            addError(errors);
        }
        if (imgGalleryData != null) {
            setImgGalleryData(imgGalleryData);
        }
    }

    public HashMap<String, ArrayList<String>> prepareResult() {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        result.put("imgLinks", getImgLinks());
        result.put("errors", getErrors());
        return result;
    }

    public void clear() {
        imgGalleryData.clear();
        imgLinks.clear();
        errors.clear();
    }
}
