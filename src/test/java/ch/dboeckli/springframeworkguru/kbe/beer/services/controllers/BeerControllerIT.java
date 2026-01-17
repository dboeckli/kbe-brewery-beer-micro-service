package ch.dboeckli.springframeworkguru.kbe.beer.services.controllers;

import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.dto.BeerPagedList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class BeerControllerIT {

    private static final String API_V1_BEER_BASE = "/api/v1/beer";

    @LocalServerPort
    private Integer port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetBeers() {
        ResponseEntity<BeerPagedList> response = restTemplate.exchange(
            API_V1_BEER_BASE,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {
            }
        );
        BeerPagedList pagedList = response.getBody();

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent()).hasSize(9);

        pagedList.getContent().forEach(beerDto -> {
            BeerDto fetchedBeerDto = restTemplate.getForObject(API_V1_BEER_BASE + "/" + beerDto.getId().toString(), BeerDto.class);
            assertThat(beerDto.getId()).isEqualByComparingTo(fetchedBeerDto.getId());
        });
    }

    @Test
    void testGetBeersRestClient() {
        RestClient restClient = RestClient.create("http://localhost:" + port);

        BeerPagedList pagedList = restClient.get()
            .uri(API_V1_BEER_BASE)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent()).hasSize(9);

        pagedList.getContent().forEach(beerDto -> {
            BeerDto fetchedBeerDto = restClient.get()
                .uri(API_V1_BEER_BASE + "/" + beerDto.getId().toString())
                .retrieve()
                .body(BeerDto.class);
            assertThat(beerDto.getId()).isEqualByComparingTo(fetchedBeerDto.getId());
        });
    }


}
