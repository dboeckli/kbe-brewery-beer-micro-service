package ch.dboeckli.springframeworkguru.kbe.beer.services.dto.events;

import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.BeerDto;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
public class NewInventoryEvent extends BeerEvent implements Serializable {

    static final long serialVersionUID = -1761313326070018802L;

    public NewInventoryEvent(BeerDto beerDto) {
        super(beerDto);
    }
}
