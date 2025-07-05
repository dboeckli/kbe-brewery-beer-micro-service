package ch.dboeckli.springframeworkguru.kbe.beer.services.controllers;

import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.dto.BeerPagedList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BeerControllerIT {

    private static final String API_V1_BEER_BASE = "/api/v1/beer";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetBeers() {
        BeerPagedList pagedList = restTemplate.getForObject(API_V1_BEER_BASE, BeerPagedList.class);

        assertThat(pagedList.getContent()).hasSize(9);

        pagedList.getContent().forEach(beerDto -> {
            BeerDto fetchedBeerDto = restTemplate.getForObject(API_V1_BEER_BASE + "/" + beerDto.getId().toString(), BeerDto.class);

            assertThat(beerDto.getId()).isEqualByComparingTo(fetchedBeerDto.getId());
        });
    }
}
