package scrapy;

import scrapy.yelpscraper.YelpRequestController;
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
    private Gson gson = new Gson();
    private YelpResult yelpResult;
    private String jwt, badJwt;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment env;

    @Mock
    private YelpRequestController yelpReqControllerMock;

    @Mock
    private Environment envMock;

    @InjectMocks
    private ApplicationController reqController;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(reqController).build();
        when(envMock.getProperty(any(String.class))).thenReturn(env.getProperty("jwt.secret"));
        Algorithm algorithm = Algorithm.HMAC256(env.getProperty("jwt.secret"));
        jwt = JWT.create().sign(algorithm);
        badJwt = jwt + "bad";
        yelpResult = new YelpResult();
    }

    @Test
    public void testPostReqWithValidJsonBody() throws Exception {
        int numLinks = 300;
        HashMap<String, ArrayList<String>> mockRes = new HashMap<>();
        ArrayList<String> mockImgLinks = TestUtils.genImgLinks(0, numLinks);
        mockRes.put("imgLinks", mockImgLinks);

        mockRes.put("errors", new ArrayList<>());

        when(yelpReqControllerMock.fetchImgLinks()).thenReturn(mockRes);

        String validJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";
        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        HashMap<String, ArrayList<String>> resData = gson.fromJson(res.getResponse().getContentAsString(), HashMap.class);
        ArrayList<String> resImgLinks = resData.get("imgLinks");
        ArrayList<String> resErrors = resData.get("errors");

        assert(res.getResponse().getStatus() == HttpStatus.OK.value());
        assert(resImgLinks.size() == numLinks);
        for (int i = 0; i < numLinks; i++) {
            assert(resImgLinks.get(i).equals(mockImgLinks.get(i)));
        }
        assert(resErrors.size() == 0);
    }

    @Test
    public void testPostReqCompletedWithErrors() throws Exception {
        int numLinks = 300;
        HashMap<String, ArrayList<String>> mockRes = new HashMap<>();
        ArrayList<String> mockImgLinks = TestUtils.genImgLinks(0, numLinks);
        mockRes.put("imgLinks", mockImgLinks);

        ArrayList<String> errors = new ArrayList<>();
        errors.add("Error type: ErrorClassName - Error message");
        mockRes.put("errors", errors);

        when(yelpReqControllerMock.fetchImgLinks()).thenReturn(mockRes);

        String validJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";
        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        HashMap<String, ArrayList<String>> resData = gson.fromJson(res.getResponse().getContentAsString(), HashMap.class);
        ArrayList<String> resImgLinks = resData.get("imgLinks");
        ArrayList<String> resErrors = resData.get("errors");

        assert(res.getResponse().getStatus() == HttpStatus.OK.value());
        assert(resImgLinks.size() == numLinks);
        for (int i = 0; i < numLinks; i++) {
            assert(resImgLinks.get(i).equals(mockImgLinks.get(i)));
        }
        assert(resErrors.size() == 1);
    }

    @Test
    public void testPostReqNotCompletedWithErrors() throws Exception {
        HashMap<String, ArrayList<String>> mockRes = new HashMap<>();
        mockRes.put("imgLinks", new ArrayList<>());

        ArrayList<String> errors = new ArrayList<>();
        errors.add("Error type: ErrorClassName - Error message");
        mockRes.put("errors", errors);

        when(yelpReqControllerMock.fetchImgLinks()).thenReturn(mockRes);

        String validJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";
        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        HashMap<String, ArrayList<String>> resData = gson.fromJson(res.getResponse().getContentAsString(), HashMap.class);
        ArrayList<String> resImgLinks = resData.get("imgLinks");
        ArrayList<String> resErrors = resData.get("errors");

        assert(res.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(resImgLinks.size() == 0);
        assert(resErrors.size() == 1);
    }

    @Test
    public void testPostReqWithInvalidJsonBody() throws Exception {
        int numLinks = 300;
        HashMap<String, ArrayList<String>> mockRes = new HashMap<>();
        mockRes.put("imgLinks", TestUtils.genImgLinks(0, numLinks));

        mockRes.put("errors", new ArrayList<>());

        when(yelpReqControllerMock.fetchImgLinks()).thenReturn(mockRes);

        String invalidUrlKeyJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";
        String invalidPicLimitKeyJson = "{\"yelpUrl\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"invalidKey\": 300 }";

        MvcResult resInvalidUrlKey = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidUrlKeyJson))
                .andReturn();

        MvcResult resInvalidPicLimitKey = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidPicLimitKeyJson))
                .andReturn();

        assert(resInvalidUrlKey.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(resInvalidPicLimitKey.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(resInvalidUrlKey.getResponse().getContentAsString().equals(""));
        assert(resInvalidPicLimitKey.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithNoJwt() throws Exception {
        String validJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";

        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andReturn();

        assert(res.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(res.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithInvalidJwt() throws Exception {
        String validJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";

        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", badJwt)
                .content(validJson))
                .andReturn();

        assert(res.getResponse().getStatus() == HttpStatus.UNAUTHORIZED.value());
        assert(res.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithNoBody() throws Exception {
        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", badJwt))
                .andReturn();

        assert(res.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(res.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithBadPath() throws Exception {
        String validJson = "{\"invalidKey\": \"https://www.yelp.com/biz/am%C3%A9lie-new-york\", \"picLimit\": 300 }";

        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/non-existent-path")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        assert(res.getResponse().getStatus() == HttpStatus.NOT_FOUND.value());
        assert(res.getResponse().getContentAsString().equals(""));
    }
}