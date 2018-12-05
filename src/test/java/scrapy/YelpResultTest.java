package scrapy;

import scrapy.yelpscraper.ScrapeResult;
import scrapy.utils.TestUtils;
import scrapy.yelpscraper.YelpResult;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YelpResultTest {
    private static final int NUM_IMGS_PER_PAGE = 30;

    YelpResult yelpResult;

    @Before
    public void setUp() {
        yelpResult = new YelpResult();
    }

    @Test
    public void testGetterAndSettersForImgGalleryData() {
        int numAllImgs = 1000;
        int numFoodImgs = 600;

        HashMap<String, Integer> galleryData = new HashMap<>();
        galleryData.put("numAllImgs", numAllImgs);
        galleryData.put("numFoodImgs", numFoodImgs);
        yelpResult.setImgGalleryData(galleryData);

        HashMap<String, Integer> actualGalleryData = yelpResult.getImgGalleryData();
        int expectedNumAllImgs = 1000;
        int expectedNumFoodImgs = 600;
        assert(actualGalleryData.get("numAllImgs") == expectedNumAllImgs);
        assert(actualGalleryData.get("numFoodImgs") == expectedNumFoodImgs);
        assert(actualGalleryData.size() == 2);
    }

    @Test
    public void testGetterAndSettersForImgLinks() {
        int NUM_IMGS_PER_PAGE = 30;
        ArrayList<String> expectedImgLinks = new ArrayList<>();
        int[] startNums = {0, 30, 60, 90, 120, 150};

        for (int i = 0; i < startNums.length; i++) {
            int start = startNums[i];
            int currNumLinks = (start/ NUM_IMGS_PER_PAGE + 1) * NUM_IMGS_PER_PAGE;

            ArrayList<String> imgLinks = TestUtils.genImgLinks(start, NUM_IMGS_PER_PAGE);
            expectedImgLinks.addAll(imgLinks);
            yelpResult.addImgLinks(start, imgLinks);
            ArrayList<String> actualImgLinks = yelpResult.getImgLinks();

            for (int j = 0; j < currNumLinks; j++) {
                assert(actualImgLinks.get(j).equals(expectedImgLinks.get(j)));
            }
            assert(actualImgLinks.size() == (start/ NUM_IMGS_PER_PAGE + 1) * NUM_IMGS_PER_PAGE);
        }
    }

    @Test
    public void testGetterAndSettersForErrors() {
        int expectedErrorSize;
        String errorMsg = "Error message 1";

        int numErrorsInArrayList = 5;
        ArrayList<String> errorMsgsInArrayList = new ArrayList<>();

        for (int i = 0; i < numErrorsInArrayList; i++) {
            errorMsgsInArrayList.add("Error message " + i);
        }

        yelpResult.addError(errorMsg);
        expectedErrorSize = 1;
        assert(yelpResult.getErrors().size() == expectedErrorSize);

        yelpResult.addError(errorMsgsInArrayList);
        expectedErrorSize += numErrorsInArrayList;
        assert(yelpResult.getErrors().size() == expectedErrorSize);
    }

    @Test
    public void testAddScrapeResult() {
        int startNum = 0;
        int numAllImgsVal = 1000;
        int numFoodImgsVal = 600;

        HashMap<String, Integer> galleryData = new HashMap<>();
        galleryData.put("numAllImgs", numAllImgsVal);
        galleryData.put("numFoodImgs", numFoodImgsVal);

        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum, NUM_IMGS_PER_PAGE);

        ArrayList<String> errors = new ArrayList<>();
        errors.add("Error message 1");

        ScrapeResult scrapeRes = new ScrapeResult(startNum, imgLinks, errors);
        scrapeRes.setImgGalleryData(galleryData);

        yelpResult.addScrapeResult(scrapeRes);

        int expectedNumAllImgsVal = 1000;
        int expectedNumFoodImgsVal = 600;
        HashMap<String, Integer> actualGalleryData = yelpResult.getImgGalleryData();
        assert(actualGalleryData.get("numAllImgs") == expectedNumAllImgsVal);
        assert(actualGalleryData.get("numFoodImgs") == expectedNumFoodImgsVal);
        assert(actualGalleryData.size() == 2);

        ArrayList<String> actualImgLinks = yelpResult.getImgLinks();
        ArrayList<String> expectedImgLinks = TestUtils.genImgLinks(startNum, NUM_IMGS_PER_PAGE);
        for (int j = 0; j < NUM_IMGS_PER_PAGE; j++) {
            assert(actualImgLinks.get(j).equals(expectedImgLinks.get(j)));
        }
        assert(actualImgLinks.size() == (startNum + 1) * NUM_IMGS_PER_PAGE);

        int expectedErrorSize = 1;
        assert(yelpResult.getErrors().size() == expectedErrorSize);
    }

    @Test
    public void testAddScrapeResultWithOnlyImgLinks() {
        int startNum = 0;

        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum, NUM_IMGS_PER_PAGE);
        ArrayList<String> errors = new ArrayList<>();

        ScrapeResult scrapeRes = new ScrapeResult(startNum, imgLinks, errors);
        yelpResult.addScrapeResult(scrapeRes);

        int expectedGalleryDataSize = 0;
        assert(yelpResult.getImgGalleryData().size() == expectedGalleryDataSize);

        int expectedErrorSize = 0;
        assert(yelpResult.getErrors().size() == expectedErrorSize);
    }

    @Test
    public void testPrepareResult() {
        int startNum = 0;
        String imgLinksKey = "imgLinks";
        String errorsKey = "errors";

        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum, NUM_IMGS_PER_PAGE);
        ArrayList<String> errors = new ArrayList<>();
        String errMsg = "Error message 1";
        errors.add(errMsg);

        yelpResult.addImgLinks(startNum, imgLinks);
        yelpResult.addError(errors);

        HashMap<String, ArrayList<String>> result = yelpResult.prepareResult();

        ArrayList<String> actualImgLinks = result.get(imgLinksKey);
        ArrayList<String> actualErrors = result.get(errorsKey);
        ArrayList<String> expectedImgLinks = TestUtils.genImgLinks(startNum, NUM_IMGS_PER_PAGE);

        assert(actualImgLinks.size() == NUM_IMGS_PER_PAGE);
        for (int i = 0; i < NUM_IMGS_PER_PAGE; i++) {
            assert(actualImgLinks.get(i).equals(expectedImgLinks.get(i)));
        }
        int expectedErrorsSize = 1;
        assert(actualErrors.size() == expectedErrorsSize);
    }

    @Test
    public void testClear() {
        int startNum = 0;
        int numAllImgsVal = 1000;
        int numFoodImgsVal = 600;

        HashMap<String, Integer> galleryData = new HashMap<>();
        galleryData.put("numAllImgs", numAllImgsVal);
        galleryData.put("numFoodImgs", numFoodImgsVal);

        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum, NUM_IMGS_PER_PAGE);
        ArrayList<String> errors = new ArrayList<>();
        String errMsg = "Error message 1";
        errors.add(errMsg);

        yelpResult.setImgGalleryData(galleryData);
        yelpResult.addImgLinks(startNum, imgLinks);
        yelpResult.addError(errors);
        yelpResult.clear();

        HashMap<String, Integer> actualGalleryData = yelpResult.getImgGalleryData();
        ArrayList<String> actualImgLinks = yelpResult.getImgLinks();
        ArrayList<String> actualErrors = yelpResult.getErrors();

        assert(actualGalleryData.size() == 0);
        assert(actualImgLinks.size() == 0);
        assert(actualErrors.size() == 0);
    }
}
