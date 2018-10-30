package scrapy;

import scrapy.yelpscraper.YelpRequestController;
import scrapy.utils.RequestBodyData;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.ArrayList;

@RestController
public class RequestController {
    @Autowired
    private Environment env;

    @PostMapping(value = "/")
    public ArrayList<String> index(@RequestHeader(value="Authorization") String token, @RequestBody String reqBodyString) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(env.getProperty("jwt.secret"));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
        } catch (JWTVerificationException exception){
            return null;
        }

        RequestBodyData reqData = parseReqBodyString(reqBodyString);
        YelpRequestController yelpRequest = new YelpRequestController(reqData);
        return yelpRequest.makeYelpRequest();
    }

    private RequestBodyData parseReqBodyString(String reqBodyString) {
        Gson gson = new Gson();
        JsonObject reqBodyJsonObj = gson.fromJson(reqBodyString, JsonObject.class);

        JsonElement url = reqBodyJsonObj.get("yelpURL");
        JsonElement picLimit = reqBodyJsonObj.get("picLimit");
        return new RequestBodyData(url.getAsString(), picLimit.getAsInt());
    }
}