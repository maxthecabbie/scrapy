package scrapy;

import scrapy.yelpscraper.YelpScraperController;
import scrapy.yelpscraper.YelpResult;
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

@RestController
public class ApplicationController {
    private YelpResult yelpResult;

    @Autowired
    private Environment env;

    @Autowired
    private YelpScraperController yelpController;

    public ApplicationController() {
        this.yelpResult = new YelpResult();
    }

    @PostMapping(value = "/")
    public ResponseEntity<String> index(
            @RequestHeader(value="Authorization") String token, @RequestBody String reqBodyString) {
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

        yelpController.setReqData(reqData);
        String responseJSON = yelpController.fetchImgLinks(yelpResult);
        return sendResponse(responseJSON);
    }

    private RequestBodyData parseReqBodyString(String reqBodyString) {
        final String ADDITIONAL_REQ = "additionalRequest";
        final int NUM_IMGS_PER_PAGE = 30;

        Gson gson = new Gson();
        String yelpUrl, type;
        int picLimit = NUM_IMGS_PER_PAGE;

        try {
            JsonObject reqBodyJsonObj = gson.fromJson(reqBodyString, JsonObject.class);
            yelpUrl = reqBodyJsonObj.get("yelpUrl").getAsString();
            type = reqBodyJsonObj.get("type").getAsString();
            if (type.equals(ADDITIONAL_REQ)) {
                picLimit = reqBodyJsonObj.get("picLimit").getAsInt();
            }
        } catch (Exception e) {
            return null;
        }
        return new RequestBodyData(yelpUrl, type, picLimit);
    }

    private ResponseEntity<String> sendResponse(String response) {
        HttpStatus status = (yelpResult.getErrors().size() > 0 && yelpResult.getImgLinks().size() == 0) ?
                HttpStatus.BAD_REQUEST : HttpStatus.OK;
        yelpResult.clear();
        return new ResponseEntity<>(response, null, status);
    }
}