package scrapy;

import scrapy.yelpscraper.YelpRequestController;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.io.IOException;

@RestController
public class RequestController {

    @RequestMapping(value = "/")
    public ArrayList<String> index() {
        YelpRequestController yelpRequest = new YelpRequestController("https://www.yelp.ca/biz/l-industrie-pizzeria-brooklyn");
        ArrayList<String> yelpImageLinks = yelpRequest.makeYelpRequest();
        return yelpImageLinks;
    }
}
