package ch.dboeckli.springframeworkguru.kbe.beer.services.web.controllers;

import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.dto.BeerPagedList;
import ch.guru.springframework.kbe.lib.dto.BeerStyleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.UUID;

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
            assert fetchedBeerDto != null;
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
            assert fetchedBeerDto != null;
            assertThat(beerDto.getId()).isEqualByComparingTo(fetchedBeerDto.getId());
        });
    }

    @Test
    void testGetBeerByUpc() {
        ResponseEntity<BeerPagedList> response = restTemplate.exchange(
            API_V1_BEER_BASE,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {
            }
        );
        BeerPagedList pagedList = response.getBody();

        assertThat(pagedList).isNotNull();
        BeerDto anyBeer = pagedList.getContent().getFirst();

        BeerDto fetchedBeer = restTemplate.getForObject("/api/v1/beerUpc/" + anyBeer.getUpc(), BeerDto.class);

        assertThat(fetchedBeer).isNotNull();
        assertThat(fetchedBeer.getUpc()).isEqualTo(anyBeer.getUpc());
        assertThat(fetchedBeer.getId()).isEqualByComparingTo(anyBeer.getId());
    }

    @Test
    void testSaveNewBeer() {
        BeerDto newBeer = BeerDto.builder()
            .beerName("IT New Beer")
            .beerStyle(BeerStyleEnum.IPA)
            .price(new java.math.BigDecimal("9.99"))
            .quantityOnHand(10)
            .upc("IT-UPC-0001")
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.postForEntity(
            API_V1_BEER_BASE,
            new HttpEntity<>(newBeer, headers),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    void testUpdateBeer() {
        UUID beerId = createBeerAndGetId("IT Update Beer", "IT-UPC-0002");

        BeerDto updateDto = BeerDto.builder()
            .beerName("IT Update Beer - Updated")
            .beerStyle(BeerStyleEnum.IPA)
            .price(new java.math.BigDecimal("11.99"))
            .quantityOnHand(12)
            .upc("IT-UPC-0002")
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.exchange(
            API_V1_BEER_BASE + "/" + beerId,
            HttpMethod.PUT,
            new HttpEntity<>(updateDto, headers),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testDeleteBeer() {
        UUID beerId = createBeerAndGetId("IT Delete Beer", "IT-UPC-0003");

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            API_V1_BEER_BASE + "/" + beerId,
            HttpMethod.DELETE,
            null,
            Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<BeerDto> getAfterDelete = restTemplate.getForEntity(
            API_V1_BEER_BASE + "/" + beerId,
            BeerDto.class
        );

        assertThat(getAfterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testSaveNewBeerBadRequest() {
        BeerDto invalidBeer = BeerDto.builder()
            .beerStyle(BeerStyleEnum.IPA)
            .price(new java.math.BigDecimal("9.99"))
            .quantityOnHand(10)
            .upc(null)
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.postForEntity(
            API_V1_BEER_BASE,
            new HttpEntity<>(invalidBeer, headers),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private UUID createBeerAndGetId(String beerName, String upc) {
        BeerDto newBeer = BeerDto.builder()
            .beerName(beerName)
            .beerStyle(BeerStyleEnum.IPA)
            .price(new java.math.BigDecimal("9.99"))
            .quantityOnHand(10)
            .upc(upc)
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.postForEntity(
            API_V1_BEER_BASE,
            new HttpEntity<>(newBeer, headers),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI location = response.getHeaders().getLocation();
        assertThat(location).isNotNull();

        String path = location.getPath();
        String idString = path.substring(path.lastIndexOf('/') + 1);

        return UUID.fromString(idString);
    }


}
