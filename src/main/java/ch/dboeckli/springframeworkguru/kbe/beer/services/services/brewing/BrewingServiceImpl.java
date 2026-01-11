package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory.BeerInventoryService;
import ch.dboeckli.springframeworkguru.kbe.beer.services.web.mappers.BeerMapper;
import ch.guru.springframework.kbe.lib.events.BrewBeerEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.dboeckli.springframeworkguru.kbe.beer.services.config.JmsConfig.BREWING_REQUEST_QUEUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrewingServiceImpl implements BrewingService {

    private final BeerInventoryService beerInventoryService;
    private final BeerRepository beerRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerMapper beerMapper;

    private final int SCHEDULE_CHECK_INTERVAL = 5000; // in milliseconds

    @Override
    @Transactional
    @Scheduled(fixedRate = SCHEDULE_CHECK_INTERVAL) //run every 5 seconds
    public void checkForLowInventory() {
        log.info("Checking Beer Inventory every {} seconds", SCHEDULE_CHECK_INTERVAL);

        List<Beer> beers = beerRepository.findAll();

        beers.forEach(beer -> {

            Integer onhandInventoryAmount = beerInventoryService.getOnhandInventory(beer.getId());

            if (beer.getMinOnHand() >= onhandInventoryAmount) {
                log.info("Current inventory amount  {} for beer {} is lower than minimum {}. Sending Event {}",
                    onhandInventoryAmount, beer.getBeerName(), beer.getMinOnHand(), BREWING_REQUEST_QUEUE);
                jmsTemplate.convertAndSend(BREWING_REQUEST_QUEUE,
                    new BrewBeerEvent(beerMapper.beerToBeerDto(beer)));
            }
        });
    }
}
