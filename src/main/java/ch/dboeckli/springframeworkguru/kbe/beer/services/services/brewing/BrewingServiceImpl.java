package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.config.JmsConfig;
import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.events.BrewBeerEvent;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory.BeerInventoryService;
import ch.dboeckli.springframeworkguru.kbe.beer.services.web.mappers.BeerMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jt on 2019-06-23.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrewingServiceImpl implements BrewingService {

    private final BeerInventoryService beerInventoryService;
    private final BeerRepository beerRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerMapper beerMapper;

    @Override
    @Transactional
    @Scheduled(fixedRate = 5000) //run every 5 seconds
    public void checkForLowInventory() {
        log.debug("Checking Beer Inventory");

        List<Beer> beers = beerRepository.findAll();

        beers.forEach(beer -> {

            Integer invQoh = beerInventoryService.getOnhandInventory(beer.getId());

            if(beer.getMinOnHand() >= invQoh ) {
                jmsTemplate.convertAndSend(JmsConfig.BREWING_REQUEST_QUEUE,
                        new BrewBeerEvent(beerMapper.beerToBeerDto(beer)));
            }
        });
    }
}
