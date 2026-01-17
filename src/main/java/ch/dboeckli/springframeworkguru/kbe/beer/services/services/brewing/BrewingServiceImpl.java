package ch.dboeckli.springframeworkguru.kbe.beer.services.services.brewing;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory.BeerInventoryService;
import ch.dboeckli.springframeworkguru.kbe.beer.services.web.mappers.BeerMapper;
import ch.guru.springframework.kbe.lib.events.BrewBeerEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrewingServiceImpl implements BrewingService {

    private final BeerInventoryService beerInventoryService;
    private final BeerRepository beerRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerMapper beerMapper;

    @Value("${sfg.brewery.queues.brewing-request}")
    String brewingRequestQueue;

    @Override
    @Transactional
    @Scheduled(cron = "${sfg.brewery.brewing-job-cron}")
    public void checkForLowInventory() {
        log.info("Checking Beer Inventory.");

        List<Beer> beers = beerRepository.findAll();

        beers.forEach(beer -> {

            Integer onhandInventoryAmount = beerInventoryService.getOnhandInventory(beer.getId());

            if (beer.getMinOnHand() >= onhandInventoryAmount) {
                log.info("Current inventory amount  {} for beer {} is lower than minimum {}. Sending Event {}",
                    onhandInventoryAmount, beer.getBeerName(), beer.getMinOnHand(), brewingRequestQueue);
                jmsTemplate.convertAndSend(brewingRequestQueue,
                    new BrewBeerEvent(beerMapper.beerToBeerDto(beer)));
            }
        });
    }
}
