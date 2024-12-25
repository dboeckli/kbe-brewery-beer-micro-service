package ch.dboeckli.springframeworkguru.kbe.beer.services.dto.events;

import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.BeerOrderDto;
import org.springframework.context.ApplicationEvent;

public class NewBeerOrderEvent extends ApplicationEvent {

    public NewBeerOrderEvent(BeerOrderDto source) {
        super(source);
    }

    public BeerOrderDto getBeerOrder(){
        return (BeerOrderDto) this.source;
    }
}
