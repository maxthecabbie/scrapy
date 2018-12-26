package scrapy;

import scrapy.yelpscraper.PaginatedScrapeResult;
import scrapy.utils.TestUtils;
import scrapy.yelpscraper.YelpResult;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.TreeMap;

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
    public void testGettingAndAddingImgLinks() {
        TreeMap<Integer, ArrayList<String>> expectedImgLinks = new TreeMap<>();
        int[] startNums = {30, 60, 90, 120, 150, 180};

        for (int i = 0; i < startNums.length; i++) {
            int start = startNums[i];
            int currNumLinks = (start/NUM_IMGS_PER_PAGE) * NUM_IMGS_PER_PAGE;

            ArrayList<String> imgLinks = TestUtils.genImgLinks(start);
            expectedImgLinks.put(start, imgLinks);
            yelpResult.addImgLinks(start, imgLinks);
            TreeMap<Integer, ArrayList<String>> actualImgLinks = yelpResult.getImgLinks();

            for (int j = 0; j < currNumLinks; j++) {
                assert(actualImgLinks.get(start).equals(expectedImgLinks.get(start)));
            }
            assert(actualImgLinks.size() == (start/NUM_IMGS_PER_PAGE));
        }
    }

    @Test
    public void testGettingAndAddingErrors() {
        int expectedErrorSize;
        String errorMsg = "A single error message";

        int numErrorsInArrayList = 5;
        ArrayList<String> errorMsgArrayList = new ArrayList<>();

        for (int i = 0; i < numErrorsInArrayList; i++) {
            errorMsgArrayList.add("Error message " + i);
        }

        yelpResult.addError(errorMsg);
        expectedErrorSize = 1;
        assert(yelpResult.getErrors().size() == expectedErrorSize);

        yelpResult.addMultipleErrors(errorMsgArrayList);
        expectedErrorSize += numErrorsInArrayList;
        assert(yelpResult.getErrors().size() == expectedErrorSize);
    }

    @Test
    public void testAddPageScrapeResult() {
        int startNum = 0;
        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum);
        ArrayList<String> errors = new ArrayList<>();
        errors.add("Error message 1");

        PaginatedScrapeResult pageScrapeRes = new PaginatedScrapeResult(startNum, imgLinks, errors);
        yelpResult.addPageScrapeResult(pageScrapeRes);

        TreeMap<Integer, ArrayList<String>> actualImgLinks= yelpResult.getImgLinks();
        TreeMap<Integer, ArrayList<String>> expectedImgLinks = new TreeMap<>();
        expectedImgLinks.put(startNum, TestUtils.genImgLinks(startNum));

        assert(actualImgLinks.get(startNum).equals(expectedImgLinks.get(startNum)));
        assert(actualImgLinks.get(startNum).size() == (startNum + 1) * NUM_IMGS_PER_PAGE);
        assert(actualImgLinks.size() == 1);

        int expectedErrorSize = 1;
        assert(yelpResult.getErrors().size() == expectedErrorSize);
    }

    @Test
    public void testAddPageScrapeResultWithOnlyImgLinks() {
        int startNum = 0;

        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum);
        ArrayList<String> errors = new ArrayList<>();

        PaginatedScrapeResult pageScrapeRes = new PaginatedScrapeResult(startNum, imgLinks, errors);
        yelpResult.addPageScrapeResult(pageScrapeRes);

        int expectedErrorSize = 0;
        assert(yelpResult.getErrors().size() == expectedErrorSize);
    }

    @Test
    public void testPrepareResult() {
        int startNum = 0;

        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum);
        ArrayList<String> errors = new ArrayList<>();
        String errMsg = "Error message 1";
        errors.add(errMsg);

        yelpResult.addImgLinks(startNum, imgLinks);
        yelpResult.addMultipleErrors(errors);

        ArrayList<String> expectedErrors = new ArrayList<>();
        expectedErrors.add(errMsg);
        String expectedJSON = TestUtils.genJsonResponse(startNum, NUM_IMGS_PER_PAGE, expectedErrors);
        String actualJSON = yelpResult.prepareJsonResult();

        assert(actualJSON.equals(expectedJSON));
    }

    @Test
    public void testClear() {
        int startNum = 0;

        ArrayList<String> imgLinks = TestUtils.genImgLinks(startNum);
        ArrayList<String> errors = new ArrayList<>();
        String errMsg = "Error message 1";
        errors.add(errMsg);

        yelpResult.addImgLinks(startNum, imgLinks);
        yelpResult.addMultipleErrors(errors);
        yelpResult.clear();

        TreeMap<Integer, ArrayList<String>> actualImgLinksMap = yelpResult.getImgLinks();
        ArrayList<String> actualErrors = yelpResult.getErrors();

        assert(actualImgLinksMap.size() == 0);
        assert(actualErrors.size() == 0);
    }
}
