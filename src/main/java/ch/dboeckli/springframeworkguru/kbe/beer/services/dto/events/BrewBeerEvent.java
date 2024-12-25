package ch.dboeckli.springframeworkguru.kbe.beer.services.dto.events;

import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.BeerDto;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
public class BrewBeerEvent extends BeerEvent implements Serializable {

    static final long serialVersionUID = 5294557463904704401L;

    public BrewBeerEvent(BeerDto beerDto) {
        super(beerDto);
    }

}
