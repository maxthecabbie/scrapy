package scrapy;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import scrapy.utils.TestUtils;
import scrapy.yelpscraper.YelpRequestController;
import scrapy.yelpscraper.YelpResult;
import scrapy.yelpscraper.Scraper;
import scrapy.yelpscraper.PaginatedScrapeResult;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YelpRequestControllerTest {
    private static final int THREAD_COUNT = 10;
    private static final int INITIAL_START_NUM = 0;
    private static final int NUM_IMGS_PER_PAGE = 30;

    @Spy
    private ExecutorCompletionService mockEcs = new ExecutorCompletionService(Executors.newFixedThreadPool(THREAD_COUNT));

    @Spy
    private YelpResult mockYelpResult;

    @Spy
    private Scraper mockYelpScraper;

    @InjectMocks
    private YelpRequestController yelpController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFetchInitialPage() throws Exception {
        int initStartNum = 0;
        int numAllImgs = 60;
        int numFoodImgs = 30;
        int picLimit = 300;

        HashMap<String, Integer> mockImgGalleryData = new HashMap<>();
        mockImgGalleryData.put("numAllImgs", numAllImgs);
        mockImgGalleryData.put("numFoodImgs", numFoodImgs);

        ArrayList<String> mockImgLinks = TestUtils.genImgLinks(initStartNum, NUM_IMGS_PER_PAGE);
        ArrayList<String> mockErrors = new ArrayList<>();
        PaginatedScrapeResult pageScrapeRes = new PaginatedScrapeResult(initStartNum, mockImgLinks, mockErrors);
        pageScrapeRes.setImgGalleryData(mockImgGalleryData);

        setupScrapeYelpMockFunc(numAllImgs, numFoodImgs);

        yelpController.setYelpUrl("https://www.yelp.com/biz/atera-new-york");
        yelpController.setPicLimit(picLimit);
        HashMap<String, ArrayList<String>> result = yelpController.fetchImgLinks();
        ArrayList<String> resImgLinks = result.get("imgLinks");
        ArrayList<String> resErrors = result.get("errors");

        assert(resImgLinks.size() == numFoodImgs);
        for (int i = 0; i < NUM_IMGS_PER_PAGE; i++) {
            assert(resImgLinks.get(i).equals(mockImgLinks.get(i)));
        }
        assert(resErrors.size() == 0);
        verify(mockYelpScraper, never()).call();
        verify(mockEcs, never()).take();
    }

    @Test
    public void testFetchMultiplePagesWithThreads() throws Exception {
        int picLimit = 300;
        int numAllImgs = 600;
        int numFoodImgs = 300;

        setupScrapeYelpMockFunc(numAllImgs, numFoodImgs);

        yelpController.setYelpUrl("https://www.yelp.com/biz/atera-new-york");
        yelpController.setPicLimit(picLimit);
        HashMap<String, ArrayList<String>> result = yelpController.fetchImgLinks();
        ArrayList<String> resImgLinks = result.get("imgLinks");
        ArrayList<String> resErrors = result.get("errors");

        ArrayList<String> expectedImgLinks = TestUtils.genImgLinks(0, numFoodImgs);
        assert(resImgLinks.size() == numFoodImgs);
        for (int i = 0; i < numFoodImgs; i++) {
            assert(resImgLinks.get(i).equals(expectedImgLinks.get(i)));
        }
        assert(resErrors.size() == 0);
        int expectedInvocations = (numFoodImgs - NUM_IMGS_PER_PAGE) / NUM_IMGS_PER_PAGE;
        verify(mockYelpScraper, Mockito.times(expectedInvocations)).call();
        verify(mockEcs, Mockito.times(expectedInvocations)).take();
    }

    @Test
    public void testInvalidUrlHost() throws Exception {
        yelpController.setYelpUrl("https://www.invalid.com/biz/atera-new-york");
        HashMap<String, ArrayList<String>> result = yelpController.fetchImgLinks();
        ArrayList<String> imgLinks = result.get("imgLinks");
        ArrayList<String> errors = result.get("errors");
        assert(imgLinks.size() == 0);
        assert(errors.size() > 0);
    }

    @Test
    public void testInvalidUrlProtocol() throws Exception {
        yelpController.setYelpUrl("http://www.yelp.com/biz/atera-new-york");
        HashMap<String, ArrayList<String>> result = yelpController.fetchImgLinks();
        ArrayList<String> imgLinks = result.get("imgLinks");
        ArrayList<String> errors = result.get("errors");
        assert(imgLinks.size() == 0);
        assert(errors.size() > 0);
    }

    @Test
    public void testUrlPathTooLong() throws Exception {
        yelpController.setYelpUrl("http://www.yelp.com/biz/atera-new-york/invalid");
        HashMap<String, ArrayList<String>> result = yelpController.fetchImgLinks();
        ArrayList<String> imgLinks = result.get("imgLinks");
        ArrayList<String> errors = result.get("errors");
        assert(imgLinks.size() == 0);
        assert(errors.size() > 0);
    }

    @Test
    public void testUrlPathTooShort() throws Exception {
        yelpController.setYelpUrl("http://www.yelp.com/atera-new-york");
        HashMap<String, ArrayList<String>> result = yelpController.fetchImgLinks();
        ArrayList<String> imgLinks = result.get("imgLinks");
        ArrayList<String> errors = result.get("errors");
        assert(imgLinks.size() == 0);
        assert(errors.size() > 0);
    }

    public void setupScrapeYelpMockFunc(int numAllImgs, int numFoodImgs) throws Exception {
        Mockito.doAnswer(new Answer<PaginatedScrapeResult>() {
            @Override
            public PaginatedScrapeResult answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                int startNum = (int) args[0];

                ArrayList<String> mockImgLinks = TestUtils.genImgLinks(startNum, NUM_IMGS_PER_PAGE);
                ArrayList<String> mockErrors = new ArrayList<>();
                PaginatedScrapeResult pageScrapeRes = new PaginatedScrapeResult(startNum, mockImgLinks, mockErrors);

                if (startNum == INITIAL_START_NUM) {
                    HashMap<String, Integer> mockImgGalleryData = new HashMap<>();
                    mockImgGalleryData.put("numAllImgs", numAllImgs);
                    mockImgGalleryData.put("numFoodImgs", numFoodImgs);

                    pageScrapeRes.setImgGalleryData(mockImgGalleryData);
                }
                return pageScrapeRes;
            }
        }).when(mockYelpScraper).scrapeYelp(any(Integer.class), any(String.class));
    }
}
