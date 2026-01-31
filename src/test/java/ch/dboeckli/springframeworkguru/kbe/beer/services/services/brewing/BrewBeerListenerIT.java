package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.guru.springframework.kbe.lib.dto.BeerStyleEnum;
import ch.guru.springframework.kbe.lib.events.BrewBeerEvent;
import ch.guru.springframework.kbe.lib.events.NewInventoryEvent;
import ch.guru.springframework.kbe.lib.dto.BeerDto;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "sfg.brewery.queues.new-inventory=new-inventory-test"
})
@Slf4j
public class BrewBeerListenerIT {

    @Autowired
    BrewBeerListener brewBeerListener;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${sfg.brewery.queues.new-inventory}")
    String newInventoryQueue;

    @BeforeEach
    void setUp() {
        beerRepository.deleteAll();
    }

    @Test
    void testBrewBeerListener() {
        // ARRANGE: Erstelle ein Beer in der DB
        Beer beer = Beer.builder()
            .beerName("Galaxy Cat")
            .beerStyle(BeerStyleEnum.PALE_ALE)
            .minOnHand(12)
            .quantityToBrew(200)
            .upc("123123123123")
            .price(new BigDecimal("12.95"))
            .build();

        Beer savedBeer = beerRepository.save(beer);

        // Erstelle ein BrewBeerEvent
        BeerDto beerDto = BeerDto.builder()
            .id(savedBeer.getId())
            .beerName(savedBeer.getBeerName())
            .beerStyle(savedBeer.getBeerStyle())
            .upc(savedBeer.getUpc())
            .price(savedBeer.getPrice())
            .build();

        BrewBeerEvent brewBeerEvent = new BrewBeerEvent(beerDto);

        // ACT: Sende das BrewBeerEvent auf die brewing-request Queue
        log.info("Sending BrewBeerEvent for beer: {}", beerDto.getId());
        brewBeerListener.listen(brewBeerEvent);

        // ASSERT: Warte auf NewInventoryEvent auf der new-inventory Queue
        NewInventoryEvent event = awaitEventInQueue(newInventoryQueue, savedBeer.getId());

        assertThat(event).isNotNull();
        assertThat(event.getBeerDto()).isNotNull();
        assertThat(event.getBeerDto().getId()).isEqualTo(savedBeer.getId());
        assertThat(event.getBeerDto().getBeerName()).isEqualTo("Galaxy Cat");
        assertThat(event.getBeerDto().getQuantityOnHand()).isEqualTo(savedBeer.getQuantityToBrew());
    }

    private NewInventoryEvent awaitEventInQueue(String queueName, UUID expectedBeerId) {
        AtomicReference<NewInventoryEvent> foundEventRef = new AtomicReference<>();

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
                            NewInventoryEvent event = objectMapper.readValue(payload, NewInventoryEvent.class);
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
