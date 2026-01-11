package ch.dboeckli.springframeworkguru.kbe.beer.services.config;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(@Value("${sfg.brewery.inventory-user}") String user,
                                                                   @Value("${sfg.brewery.inventory-password}") String password) {


        return new BasicAuthRequestInterceptor(user, password);
    }
}
