package scrapy.yelpscraper;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class YelpResult {
    private TreeMap<Integer, ArrayList<String>> imgLinks;
    private ArrayList<String> errors;

    public YelpResult() {
        imgLinks = new TreeMap<>();
        errors = new ArrayList<>();
    }

    public TreeMap<Integer, ArrayList<String>> getImgLinks() {
        return imgLinks;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void addImgLinks(int key, ArrayList<String> linksFromScrape) {
        this.imgLinks.put(key, linksFromScrape);
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void addMultipleErrors(ArrayList<String> errorList) {
        errors.addAll(errorList);
    }

    public void addPageScrapeResult(PaginatedScrapeResult pageScrapeRes) {
        int startNum = pageScrapeRes.getStartNum();
        ArrayList<String> imgLinks = pageScrapeRes.getImgLinks();
        ArrayList<String> errors = pageScrapeRes.getErrors();

        addImgLinks(startNum, imgLinks);

        if (errors.size() > 0) {
            addMultipleErrors(errors);
        }
    }

    public String prepareJsonResult() {
        Gson gson = new Gson();
        HashMap<String, ArrayList<String>> result = new HashMap<>();

        ArrayList<String> imgLinksArrayList = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<String>> entry : imgLinks.entrySet()) {
            imgLinksArrayList.addAll(entry.getValue());
        }

        result.put("imgLinks", imgLinksArrayList);
        result.put("errors", errors);
        return gson.toJson(result);
    }

    public void clear() {
        imgLinks.clear();
        errors.clear();
    }
}
