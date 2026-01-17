package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.events.BrewBeerEvent;
import ch.guru.springframework.kbe.lib.events.NewInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class BrewBeerListener {

    private final JmsTemplate jmsTemplate;
    private final BeerRepository beerRepository;

    @Value("${sfg.brewery.queues.new-inventory}")
    String newInventoryQueue;

    @Transactional
    @JmsListener(destination = "${sfg.brewery.queues.brewing-request}")
    public void listen(BrewBeerEvent brewBeerEvent) {
        log.info("Received Brew Beer Request for beer: {}", brewBeerEvent.getBeerDto());
        BeerDto dto = brewBeerEvent.getBeerDto();

        Beer beer = beerRepository.getReferenceById(dto.getId());
        //Brewing some beer
        dto.setQuantityOnHand(beer.getQuantityToBrew());

        NewInventoryEvent newInventoryEvent = new NewInventoryEvent(dto);
        log.info("Sending New Inventory Event for beer: {}", dto);
        jmsTemplate.convertAndSend(newInventoryQueue, newInventoryEvent);
    }
}
