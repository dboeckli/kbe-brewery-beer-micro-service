package ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory;

import ch.guru.springframework.kbe.lib.dto.BeerInventoryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Primary // Damit dieser Service immer genommen wird (ersetzt Profile-Logik)
public class BeerInventoryServiceRestClientImpl implements BeerInventoryService {

    public static final String INVENTORY_PATH = "/api/v1/beer/{beerId}/inventory";

    private final RestClient restClient;
    private final RestClient failoverRestClient;

    public BeerInventoryServiceRestClientImpl(RestClient.Builder builder,
                                              @Value("${sfg.brewery.inventory-user}") String inventoryUser,
                                              @Value("${sfg.brewery.inventory-password}") String inventoryPassword,
                                              @Value("${sfg.brewery.beer-inventory-service-host}") String beerInventoryServiceHost,
                                              @Value("${sfg.brewery.inventory-failover-service-host}") String failoverServiceHost) {

        // RestClient konfigurieren mit Basic Auth
        this.restClient = builder
            .baseUrl(beerInventoryServiceHost)
            .defaultHeaders(headers -> headers.setBasicAuth(inventoryUser, inventoryPassword))
            .build();

        this.failoverRestClient = builder
            .baseUrl(failoverServiceHost)
            .defaultHeaders(headers -> headers.setBasicAuth(inventoryUser, inventoryPassword))
            .build();
    }

    @Override
    public Integer getOnhandInventory(UUID beerId) {
        log.info("Calling Inventory Service (RestClient) - BeerId: " + beerId);

        try {
            List<BeerInventoryDto> inventoryList = restClient.get()
                .uri(INVENTORY_PATH, beerId) // URI Template Variablen werden automatisch ersetzt
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

            Integer onHand = inventoryList.stream()
                .mapToInt(BeerInventoryDto::getQuantityOnHand)
                .sum();

            log.info("BeerId: " + beerId + " On hand is: " + onHand);
            return onHand;

        } catch (Exception e) {
            log.error("Error calling inventory service. Calling Failover Service.", e);

            try {
                List<BeerInventoryDto> inventoryList = failoverRestClient.get()
                    .uri("/inventory-failover")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

                Integer onHand = inventoryList.stream()
                    .mapToInt(BeerInventoryDto::getQuantityOnHand)
                    .sum();

                log.info("Failover calling successful. BeerId: " + beerId + " On hand is: " + onHand);
                return onHand;
            } catch (Exception ex) {
                log.error("Error calling failover service", ex);
                return 0;
            }
        }
    }


}
