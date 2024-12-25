package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.config.JmsConfig;
import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.BeerDto;
import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.events.BrewBeerEvent;
import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.events.NewInventoryEvent;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jt on 2019-06-24.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BrewBeerListener {

    private final JmsTemplate jmsTemplate;
    private final BeerRepository beerRepository;

    @Transactional
    @JmsListener(destination = JmsConfig.BREWING_REQUEST_QUEUE)
    public void listen(BrewBeerEvent brewBeerEvent){

        BeerDto dto = brewBeerEvent.getBeerDto();

        Beer beer = beerRepository.getOne(dto.getId());
        //Brewing some beer
        dto.setQuantityOnHand(beer.getQuantityToBrew());

        NewInventoryEvent newInventoryEvent = new NewInventoryEvent(dto);

        jmsTemplate.convertAndSend(JmsConfig.NEW_INVENTORY_QUEUE, newInventoryEvent);
    }
}
