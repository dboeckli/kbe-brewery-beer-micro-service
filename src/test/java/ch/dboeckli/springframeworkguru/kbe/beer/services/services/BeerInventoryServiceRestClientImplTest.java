
package ch.dboeckli.springframeworkguru.kbe.beer.services.services;

import ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory.BeerInventoryServiceRestClientImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BeerInventoryServiceRestClientImpl.class)
@TestPropertySource(properties = {
    "spring.docker.compose.skip.in-tests=true"
})
class BeerInventoryServiceRestClientImplTest {

    @Autowired
    MockRestServiceServer server;

    @Autowired
    BeerInventoryServiceRestClientImpl beerInventoryService;

    @Test
    void getOnhandInventory_Failover() {
        UUID beerId = UUID.randomUUID();
        // Simulierter JSON-Response vom Failover-Service
        String failoverResponse = "[{\"id\":\"" + UUID.randomUUID() + "\", \"quantityOnHand\": 999}]";

        // 1. Erwartung: Aufruf an den Haupt-Service schlägt fehl (500 Internal Server Error)
        server.expect(requestTo("http://localhost:8082/api/v1/beer/" + beerId + "/inventory"))
                .andRespond(withServerError());

        // 2. Erwartung: Daraufhin erfolgt der Aufruf an den Failover-Service, der erfolgreich antwortet
        server.expect(requestTo("http://localhost:8083/inventory-failover"))
                .andRespond(withSuccess(failoverResponse, MediaType.APPLICATION_JSON));

        // Test-Ausführung
        Integer onHand = beerInventoryService.getOnhandInventory(beerId);

        // Assertion: Wir erwarten den Wert aus dem Failover-Response (999)
        assertEquals(999, onHand);
    }

    @Test
    void getOnhandInventory_Success_NoFailover() {
        UUID beerId = UUID.randomUUID();
        String primaryResponse = "[{\"id\":\"" + UUID.randomUUID() + "\", \"quantityOnHand\": 50}]";

        // Erwartung: Haupt-Service antwortet direkt erfolgreich
        server.expect(requestTo("http://localhost:8082/api/v1/beer/" + beerId + "/inventory"))
            .andRespond(withSuccess(primaryResponse, MediaType.APPLICATION_JSON));

        Integer onHand = beerInventoryService.getOnhandInventory(beerId);

        assertEquals(50, onHand);
    }
}
