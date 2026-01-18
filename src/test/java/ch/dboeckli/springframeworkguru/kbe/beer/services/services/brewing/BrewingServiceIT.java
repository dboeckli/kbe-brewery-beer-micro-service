package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.dboeckli.springframeworkguru.kbe.beer.services.services.beer.BeerServiceImpl;
import ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory.BeerInventoryService;
import ch.guru.springframework.kbe.lib.dto.BeerStyleEnum;
import ch.guru.springframework.kbe.lib.events.BrewBeerEvent;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
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

    @Value("${sfg.brewery.brewing-job-cron}")
    String cron;

    @MockitoBean
    BrewBeerListener brewBeerListener;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        beerRepository.deleteAll();
    }

    @Test
    void testBrewingEventFlow() throws Exception {
        assertEquals("-", cron); // disabled

        Beer beer = Beer.builder()
            .beerName("Pilgrim")
            .beerStyle(BeerStyleEnum.IPA)
            .minOnHand(12) // Wir brauchen mindestens 12
            .quantityToBrew(200)
            .upc("123123123123")
            .price(new BigDecimal("12.95"))
            .build();

        Beer savedBeer = beerRepository.save(beer);
        cacheManager.getCache(BeerServiceImpl.CACHE_NAME).clear();

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
        BrewBeerEvent event = awaitEventInQueue(brewingRequestQueue, savedBeer.getId());

        assertThat(event).isNotNull();
        assertThat(event.getBeerDto().getQuantityOnHand()).isNull();
        assertThat(event.getBeerDto().getId()).isEqualTo(savedBeer.getId());
    }

    private BrewBeerEvent awaitEventInQueue(String queueName, UUID expectedBeerId) {
        AtomicReference<BrewBeerEvent> foundEventRef = new AtomicReference<>();

        // Wir setzen ein kurzes Timeout für den JMS-Receive, damit Awaitility die Schleife steuern kann
        jmsTemplate.setReceiveTimeout(100);

        try {
            Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    Message message = jmsTemplate.receive(queueName);

                    if (message instanceof TextMessage textMessage) {
                        try {
                            String payload = textMessage.getText();
                            BrewBeerEvent event = objectMapper.readValue(payload, BrewBeerEvent.class);
                            log.info("Got event: {}", event);

                            if (event.getBeerDto() != null && expectedBeerId.equals(event.getBeerDto().getId())) {
                                foundEventRef.set(event);
                                return true; // Gefunden!
                            } else {
                                log.debug("Ignoriere Nachricht für andere ID: {}", event.getBeerDto().getId());
                            }
                        } catch (Exception e) {
                            log.warn("Konnte Nachricht nicht deserialisieren: {}", e.getMessage());
                        }
                    }
                    return false; // Weiter suchen
                });
        } catch (Exception e) {
            // Awaitility wirft eine Exception bei Timeout -> wir geben null zurück oder lassen den Test hier failen
            return null;
        }

        return foundEventRef.get();
    }
}
