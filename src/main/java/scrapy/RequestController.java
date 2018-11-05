package scrapy;

import scrapy.yelpscraper.YelpRequestController;
import scrapy.yelpscraper.YelpRequestResult;
import scrapy.utils.RequestBodyData;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class RequestController {
    @Autowired
    private Environment env;

    @PostMapping(value = "/")
    public ResponseEntity<HashMap<String, ArrayList<String>>> index(@RequestHeader(value="Authorization") String token, @RequestBody String reqBodyString) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(env.getProperty("jwt.secret"));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
        } catch (JWTVerificationException exception){
            return new ResponseEntity<>(null, null, HttpStatus.UNAUTHORIZED);
        }

        RequestBodyData reqData = parseReqBodyString(reqBodyString);
        if (reqData == null) {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }

        YelpRequestController yelpController = new YelpRequestController(reqData);
        YelpRequestResult yelpResult = yelpController.makeYelpRequest();
        return formatResponse(yelpResult);
    }

    private RequestBodyData parseReqBodyString(String reqBodyString) {
        Gson gson = new Gson();
        String yelpURL;
        int picLimit;

        try {
            JsonObject reqBodyJsonObj = gson.fromJson(reqBodyString, JsonObject.class);
            yelpURL = reqBodyJsonObj.get("yelpURL").getAsString();
            picLimit = reqBodyJsonObj.get("picLimit").getAsInt();
        } catch (Exception e) {
            return null;
        }
        return new RequestBodyData(yelpURL, picLimit);
    }

    private ResponseEntity<HashMap<String, ArrayList<String>>> formatResponse(YelpRequestResult yelpResult) {
        HashMap<String, ArrayList<String>> response = new HashMap<>();
        response.put("imgLinks", yelpResult.getImgLinks());
        response.put("errors", yelpResult.getErrors());
        HttpStatus status = (yelpResult.getErrors().size() > 0 && yelpResult.getImgLinks().size() == 0) ?
                HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return new ResponseEntity<>(response, null, status);
    }
}