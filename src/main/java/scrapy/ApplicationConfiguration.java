package scrapy;

import scrapy.yelpscraper.YelpScraperController;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public YelpScraperController yelpController(ExecutorCompletionService ecs) {
        return new YelpScraperController(ecs);
    }

}
