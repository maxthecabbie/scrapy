package scrapy;

import scrapy.yelpscraper.YelpScraperController;
import scrapy.yelpscraper.YelpResult;
import scrapy.utils.TestUtils;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.HttpStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationControllerTest {
    private static final int NUM_IMGS_PER_PAGE = 30;
    private Gson gson = new Gson();
    private String jwt, badJwt;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment env;

    @Mock
    private Environment mockEnv;

    @Mock
    private YelpResult yelpresult;

    @Mock
    private YelpScraperController yelpReqControllerMock;

    @InjectMocks
    private ApplicationController reqController;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(reqController).build();
        when(mockEnv.getProperty(any(String.class))).thenReturn(env.getProperty("jwt.secret"));
        Algorithm algorithm = Algorithm.HMAC256(env.getProperty("jwt.secret"));
        jwt = JWT.create().sign(algorithm);
        badJwt = jwt + "bad";
    }

    @Test
    public void testPostReqWithValidJsonBody() throws Exception {
        int startNum = 0;
        int numFoodImgs = 30;
        ArrayList<String> errors = new ArrayList<>();

        String mockJsonResult = TestUtils.genJsonResponse(startNum, numFoodImgs, errors);
        when(yelpReqControllerMock.fetchImgLinks(any(YelpResult.class))).thenReturn(mockJsonResult);

        String validJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", " +
                "\"picLimit\": 30," +
                "\"type\": \"initialRequest\"}";
        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        HashMap<String, ArrayList<String>> res = gson.fromJson(req.getResponse().getContentAsString(), HashMap.class);
        ArrayList<String> respImgLinks = res.get("imgLinks");
        ArrayList<String> respErrors = res.get("errors");

        ArrayList<String> expectedImgLinks = TestUtils.genImgLinks(startNum);
        assert(req.getResponse().getStatus() == HttpStatus.OK.value());
        assert(respImgLinks.size() == numFoodImgs);
        for (int i = 0; i < numFoodImgs; i++) {
            assert(respImgLinks.get(i).equals(expectedImgLinks.get(i)));
        }
        assert(respErrors.size() == 0);
    }

    @Test
    public void testPostReqCompletedWithErrors() throws Exception {
        int startNum = 30;
        int numFoodImgs = 300;
        ArrayList<String> errors = new ArrayList<>();
        errors.add("Error message 1");

        String mockJsonResult = TestUtils.genJsonResponse(startNum, numFoodImgs, errors);
        when(yelpReqControllerMock.fetchImgLinks(any(YelpResult.class))).thenReturn(mockJsonResult);

        String validJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", " +
                "\"picLimit\": 300," +
                "\"type\": \"additionalRequest\"}";
        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        HashMap<String, ArrayList<String>> res = gson.fromJson(req.getResponse().getContentAsString(), HashMap.class);
        ArrayList<String> respImgLinks = res.get("imgLinks");
        ArrayList<String> respErrors = res.get("errors");

        ArrayList<String> expectedImgLinks = new ArrayList<>();
        for (int i = 0; i < numFoodImgs; i += NUM_IMGS_PER_PAGE) {
            expectedImgLinks.addAll(TestUtils.genImgLinks(startNum + i));
        }
        assert(req.getResponse().getStatus() == HttpStatus.OK.value());
        assert(respImgLinks.size() == numFoodImgs);
        for (int i = 0; i < numFoodImgs; i++) {
            assert(respImgLinks.get(i).equals(expectedImgLinks.get(i)));
        }
        int expectedErrorNum = 1;
        assert(respErrors.size() == expectedErrorNum);
    }

    @Test
    public void testPostReqNotCompletedWithErrors() throws Exception {
        ArrayList<String> errors = new ArrayList<>();
        String errMsg1 = "\"Error message 1\"";
        String errMsg2 = "\"Error message 2\"";
        errors.add(errMsg1);
        errors.add(errMsg2);

        String mockJsonResult = String.format("{\"imgLinks\":[],\"errors\":[%s, %s]}", errMsg1, errMsg2);
        when(yelpReqControllerMock.fetchImgLinks(any(YelpResult.class))).thenReturn(mockJsonResult);
        when(yelpresult.getErrors()).thenReturn(errors);
        String validJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", " +
                "\"picLimit\": 300," +
                "\"type\": \"additionalRequest\"}";
        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        HashMap<String, ArrayList<String>> res = gson.fromJson(req.getResponse().getContentAsString(), HashMap.class);
        ArrayList<String> respImgLinks = res.get("imgLinks");
        ArrayList<String> respErrors = res.get("errors");

        int expectedImgLinksSize = 0;
        int expectedErrorsSize = errors.size();
        assert(req.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(respImgLinks.size() == expectedImgLinksSize);
        assert(respErrors.size() == expectedErrorsSize);
    }

    @Test
    public void testPostReqWithInvalidJsonBody() throws Exception {
        String invalidUrlKeyJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";
        String invalidPicLimitKeyJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"invalidKey\": 300 }";

        MvcResult invalidUrlKeyReq = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidUrlKeyJson))
                .andReturn();

        MvcResult invalidPicLimitKeyReq = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidPicLimitKeyJson))
                .andReturn();

        assert(invalidUrlKeyReq.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(invalidPicLimitKeyReq.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(invalidUrlKeyReq.getResponse().getContentAsString().equals(""));
        assert(invalidPicLimitKeyReq.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithNoJwt() throws Exception {
        String validJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";

        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andReturn();

        assert(req.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(req.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithInvalidJwt() throws Exception {
        String validJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";

        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", badJwt)
                .content(validJson))
                .andReturn();

        assert(req.getResponse().getStatus() == HttpStatus.UNAUTHORIZED.value());
        assert(req.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithNoBody() throws Exception {
        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt))
                .andReturn();

        assert(req.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(req.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithBadPath() throws Exception {
        String validJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";

        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/non-existent-path")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        assert(req.getResponse().getStatus() == HttpStatus.NOT_FOUND.value());
        assert(req.getResponse().getContentAsString().equals(""));
    }
}