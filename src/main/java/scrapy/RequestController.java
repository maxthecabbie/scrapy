package scrapy;

import scrapy.yelpscraper.YelpRequestController;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;


@RestController
public class RequestController {

    @PostMapping(value = "/")
    public ArrayList<String> index(@RequestBody String reqBodyString) {
        HashMap<String, String> requestBody = parseReqBodyString(reqBodyString);
        String yelpURL = requestBody.get("yelpURL");

        YelpRequestController yelpRequest = new YelpRequestController(yelpURL);
        ArrayList<String> yelpImageLinks = yelpRequest.makeYelpRequest();
        return yelpImageLinks;
    }

    private HashMap<String, String> parseReqBodyString(String reqBodyString) {
        HashMap<String, String> requestBody = new HashMap<>();
        Gson gson = new Gson();
        JsonObject reqBodyJsonObj = gson.fromJson(reqBodyString, JsonObject.class);

        JsonElement url = reqBodyJsonObj.get("yelpURL");
        requestBody.put("yelpURL", url.getAsString());
        return requestBody;
    }
}
