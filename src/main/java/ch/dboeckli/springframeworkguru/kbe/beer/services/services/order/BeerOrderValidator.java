package ch.dboeckli.springframeworkguru.kbe.beer.services.services.order;

import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.BeerOrderDto;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jt on 2019-09-08.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidator {

    private final BeerRepository beerRepository;

    public Boolean validateOrder(BeerOrderDto beerOrderDto){

        AtomicInteger beersNotFound = new AtomicInteger();

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (beerRepository.findByUpc(beerOrderLineDto.getUpc()) == null){
                beersNotFound.incrementAndGet();
            }
        });

        //fail order if UPC not found
        return beersNotFound.get() == 0;
    }
}
