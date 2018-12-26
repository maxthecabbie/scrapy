package scrapy;

import scrapy.utils.RequestBodyData;
import scrapy.utils.TestUtils;
import scrapy.yelpscraper.YelpScraperController;
import scrapy.yelpscraper.YelpResult;
import scrapy.yelpscraper.PageScraper;
import scrapy.yelpscraper.PaginatedScrapeResult;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YelpPageScraperControllerTest {
    private static final int THREAD_COUNT = 10;
    private static final int ADDITIONAL_START_NUM = 30;
    private static final int NUM_IMGS_PER_PAGE = 30;

    private YelpResult yelpResult;

    @Spy
    private ExecutorCompletionService mockEcs = new ExecutorCompletionService(Executors.newFixedThreadPool(THREAD_COUNT));

    @Spy
    private PageScraper mockYelpPageScraper;

    @InjectMocks
    private YelpScraperController yelpController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        yelpResult = new YelpResult();
    }

    @Test
    public void testInitialImgFetch() throws Exception {
        int startNum = 0;
        int numFoodImgs = 300;

        setupScrapeYelpMockFunc(numFoodImgs);

        String yelpUrl = "https://www.yelp.com/biz/atera-new-york";
        String type = "initialRequest";
        int picLimit = 30;
        RequestBodyData reqData = new RequestBodyData(yelpUrl, type, picLimit);
        yelpController.setReqData(reqData);

        String actualJSON = yelpController.fetchImgLinks(yelpResult);
        ArrayList<String> expectedErrors = new ArrayList<>();
        String expectedJSON = TestUtils.genJsonResponse(startNum, NUM_IMGS_PER_PAGE, expectedErrors);

        assert(actualJSON.equals(expectedJSON));
        verify(mockYelpPageScraper, never()).call();
        verify(mockEcs, never()).take();
    }

    @Test
    public void testAdditionalImgFetch() throws Exception {
        int startNum = 30;
        int numFoodImgs = 300;

        setupScrapeYelpMockFunc(numFoodImgs);

        String yelpUrl = "https://www.yelp.com/biz/atera-new-york";
        String type = "additionalRequest";
        int picLimit = 300;
        RequestBodyData reqData = new RequestBodyData(yelpUrl, type, picLimit);
        yelpController.setReqData(reqData);

        String actualJSON = yelpController.fetchImgLinks(yelpResult);
        ArrayList<String> expectedErrors = new ArrayList<>();
        String expectedJSON = TestUtils.genJsonResponse(startNum, numFoodImgs, expectedErrors);

        assert(actualJSON.equals(expectedJSON));
        int expectedInvocations = numFoodImgs/NUM_IMGS_PER_PAGE;
        verify(mockYelpPageScraper, Mockito.times(expectedInvocations)).call();
        verify(mockEcs, Mockito.times(expectedInvocations)).take();
    }

    @Test
    public void testInvalidUrlHost() throws Exception {
        String yelpUrl = "https://www.invalid.com/biz/atera-new-york";
        String type = "initialRequest";
        int picLimit = 30;
        RequestBodyData reqData = new RequestBodyData(yelpUrl, type, picLimit);
        yelpController.setReqData(reqData);

        String resultJSON = yelpController.fetchImgLinks(yelpResult);
        assert(yelpResult.getImgLinks().size() == 0);
        assert(yelpResult.getErrors().size() > 0);
        assertNull(resultJSON);
    }

    @Test
    public void testInvalidUrlProtocol() throws Exception {
        String yelpUrl = "http://www.yelp.com/biz/atera-new-york";
        String type = "initialRequest";
        int picLimit = 30;
        RequestBodyData reqData = new RequestBodyData(yelpUrl, type, picLimit);
        yelpController.setReqData(reqData);

        String resultJSON = yelpController.fetchImgLinks(yelpResult);
        assert(yelpResult.getImgLinks().size() == 0);
        assert(yelpResult.getErrors().size() > 0);
        assertNull(resultJSON);
    }

    @Test
    public void testUrlPathTooLong() throws Exception {
        String yelpUrl = "http://www.yelp.com/biz/atera-new-york/invalid";
        String type = "initialRequest";
        int picLimit = 30;
        RequestBodyData reqData = new RequestBodyData(yelpUrl, type, picLimit);
        yelpController.setReqData(reqData);

        String resultJSON = yelpController.fetchImgLinks(yelpResult);
        assert(yelpResult.getImgLinks().size() == 0);
        assert(yelpResult.getErrors().size() > 0);
        assertNull(resultJSON);
    }

    @Test
    public void testUrlPathTooShort() throws Exception {
        String yelpUrl = "http://www.yelp.com/atera-new-york";
        String type = "initialRequest";
        int picLimit = 30;
        RequestBodyData reqData = new RequestBodyData(yelpUrl, type, picLimit);
        yelpController.setReqData(reqData);

        String resultJSON = yelpController.fetchImgLinks(yelpResult);
        assert(yelpResult.getImgLinks().size() == 0);
        assert(yelpResult.getErrors().size() > 0);
        assertNull(resultJSON);
    }

    public void setupScrapeYelpMockFunc(int numFoodImgs) throws Exception {
        Mockito.doAnswer(new Answer<PaginatedScrapeResult>() {
            @Override
            public PaginatedScrapeResult answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                int startNum = (int) args[0];

                ArrayList<String> mockImgLinks = TestUtils.genImgLinks(startNum);
                ArrayList<String> mockErrors = new ArrayList<>();
                PaginatedScrapeResult pageScrapeRes = new PaginatedScrapeResult(startNum, mockImgLinks, mockErrors);

                if (startNum == ADDITIONAL_START_NUM) {
                    pageScrapeRes.setNumFoodImgs(numFoodImgs);
                }
                return pageScrapeRes;
            }
        }).when(mockYelpPageScraper).scrapeYelp(any(Integer.class), any(String.class));
    }
}
