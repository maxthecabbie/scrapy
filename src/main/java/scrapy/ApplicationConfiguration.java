package scrapy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scrapy.yelpscraper.YelpRequestController;
import scrapy.yelpscraper.YelpResult;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {
    private static final int THREAD_COUNT = 10;

    @Bean
    public ExecutorCompletionService ecs() {
        return new ExecutorCompletionService(Executors.newFixedThreadPool(THREAD_COUNT));
    }

    @Bean
    public YelpResult yelpResult() {
        return new YelpResult();
    }

    @Bean
    public YelpRequestController yelpController(ExecutorCompletionService ecs, YelpResult yelpResult) {
        return new YelpRequestController(ecs, yelpResult);
    }

}
