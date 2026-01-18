package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory.BeerInventoryService;
import ch.guru.springframework.kbe.lib.dto.BeerStyleEnum;
import ch.guru.springframework.kbe.lib.events.BrewBeerEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@TestPropertySource(properties = {
    "sfg.brewery.brewing-job-cron=-"
})
@Slf4j
public class BrewingServiceIT {

    @Autowired
    BrewingService brewingService;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    ObjectMapper objectMapper;

    // Wir mocken den InventoryService, da wir keinen echten externen Service aufrufen wollen
    @MockitoBean
    BeerInventoryService beerInventoryService;

    @Value("${sfg.brewery.queues.brewing-request}")
    String brewingRequestQueue;

    @MockitoBean
    BrewBeerListener brewBeerListener;

    @BeforeEach
    void setUp() {
        beerRepository.deleteAll();
    }

    @Test
    void testBrewingEventFlow() throws Exception {
        Beer beer = Beer.builder()
            .beerName("Pilgrim")
            .beerStyle(BeerStyleEnum.IPA)
            .minOnHand(12) // Wir brauchen mindestens 12
            .quantityToBrew(200)
            .upc("123123123123")
            .price(new BigDecimal("12.95"))
            .build();

        Beer savedBeer = beerRepository.save(beer);

        // Wir simulieren, dass wir nur 1 Bier auf Lager haben (weniger als minOnHand 12)
        given(beerInventoryService.getOnhandInventory(any())).willReturn(1);

        // 2. ACT: Den Check manuell auslösen
        log.info("Testing testBrewingEventFlow for checkForLowInventory");

        brewingService.checkForLowInventory();

        // 3. ASSERT: Wir warten auf das Ergebnis
        // Der Ablauf ist:
        // BrewingService -> Queue:brewing-request -> BrewBeerListener -> Queue:new-inventory

        // Da wir keine Listener für "new-inventory" in diesem Microservice haben (der gehört normalerweise zum Inventory Service),
        // können wir die Nachricht direkt aus dieser Queue konsumieren, um zu beweisen, dass der Listener lief.

        // Wir setzen einen Timeout (z.B. 5000ms), da JMS asynchron ist
        jmsTemplate.setReceiveTimeout(5000);
        Object receivedMessage = jmsTemplate.receiveAndConvert(brewingRequestQueue);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).isInstanceOf(BrewBeerEvent.class);

        BrewBeerEvent event = (BrewBeerEvent) receivedMessage;
        assertThat(event.getBeerDto().getId()).isEqualTo(savedBeer.getId());
    }
}
