package scrapy.utils;

import java.util.ArrayList;

public class TestUtils {
    public static ArrayList<String> genImgLinks(int startId, int numLinks) {
        ArrayList<String> imgLinks = new ArrayList<>();
        for (int i = 0; i < numLinks; i++) {
            imgLinks.add("https://s3-media4.fl.yelpcdn.com/bphoto/" + (startId + i) + "/258s.jpg");
        }
        return imgLinks;
    }
}
