package scrapy;

import scrapy.yelpscraper.YelpRequestController;
import scrapy.Utils.RequestBodyData;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.util.ArrayList;

@RestController
public class RequestController {

    @PostMapping(value = "/")
    public ArrayList<String> index(@RequestBody String reqBodyString) {
        RequestBodyData reqData = parseReqBodyString(reqBodyString);

        YelpRequestController yelpRequest = new YelpRequestController(reqData);
        ArrayList<String> yelpImageLinks = yelpRequest.makeYelpRequest();
        return yelpImageLinks;
    }

    private RequestBodyData parseReqBodyString(String reqBodyString) {
        Gson gson = new Gson();
        JsonObject reqBodyJsonObj = gson.fromJson(reqBodyString, JsonObject.class);

        JsonElement url = reqBodyJsonObj.get("yelpURL");
        JsonElement picLimit = reqBodyJsonObj.get("picLimit");
        RequestBodyData reqData = new RequestBodyData(url.getAsString(), picLimit.getAsInt());
        return reqData;
    }
}