package scrapy.utils;

import scrapy.yelpscraper.YelpResult;

import java.util.ArrayList;

public class TestUtils {
    private static final int NUM_IMGS_PER_PAGE = 30;

    public static ArrayList<String> genImgLinks(int startId) {
        ArrayList<String> imgLinks = new ArrayList<>();

        for (int i = 0; i < NUM_IMGS_PER_PAGE; i++) {
            imgLinks.add("https://s3-media4.fl.yelpcdn.com/bphoto/" + (startId + i) + "/258s.jpg");
        }
        return imgLinks;
    }

    public static String genJsonResponse(int start, int numLinks, ArrayList<String> errors) {
        YelpResult yelpResult = new YelpResult();
        final int INITIAL_REQ_START_NUM = 0;

        if (errors.size() > 0) {
            yelpResult.addMultipleErrors(errors);
        }
        for (int i = start; i <= numLinks; i += NUM_IMGS_PER_PAGE) {
            yelpResult.addImgLinks(i, genImgLinks(i));
            if (i == INITIAL_REQ_START_NUM) {
                break;
            }
        }

        return yelpResult.prepareJsonResult();
    }
}
