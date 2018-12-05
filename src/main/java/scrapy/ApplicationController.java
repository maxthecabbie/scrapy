package scrapy;

import scrapy.yelpscraper.YelpRequestController;
import scrapy.yelpscraper.YelpResult;
import scrapy.utils.RequestBodyData;

import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@RestController
public class ApplicationController {
    private static final int THREAD_COUNT = 10;

    @Autowired
    private Environment env;

    @Autowired
    private YelpRequestController yelpController;

    @Autowired
    private YelpResult yelpResult;
    
    @Bean
    public YelpRequestController yelpController() {
        ExecutorCompletionService ecs = new ExecutorCompletionService(Executors.newFixedThreadPool(THREAD_COUNT));
        return new YelpRequestController(ecs, yelpResult);
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

        yelpController.setYelpUrl(reqData.getUrl());
        yelpController.setPicLimit(reqData.getPicLimit());
        HashMap<String, ArrayList<String>> yelpData = yelpController.fetchImgLinks();
        return sendResponse(yelpData);
    }

    private RequestBodyData parseReqBodyString(String reqBodyString) {
        Gson gson = new Gson();
        String yelpUrl;
        int picLimit;
        try {
            JsonObject reqBodyJsonObj = gson.fromJson(reqBodyString, JsonObject.class);
            yelpUrl = reqBodyJsonObj.get("yelpUrl").getAsString();
            picLimit = reqBodyJsonObj.get("picLimit").getAsInt();
        } catch (Exception e) {
            return null;
        }
        return new RequestBodyData(yelpUrl, picLimit);
    }

    private ResponseEntity<String> sendResponse(HashMap<String, ArrayList<String>> yelpData) {
        HttpStatus status = (yelpData.get("errors").size() > 0 && yelpData.get("imgLinks").size() == 0) ?
                HttpStatus.BAD_REQUEST : HttpStatus.OK;
        Gson gson = new Gson();
        String responseJson = gson.toJson(yelpData);

        yelpResult.clear();
        return new ResponseEntity<>(responseJson, null, status);
    }
}