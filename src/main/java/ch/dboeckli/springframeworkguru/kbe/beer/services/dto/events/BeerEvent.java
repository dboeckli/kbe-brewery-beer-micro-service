package ch.dboeckli.springframeworkguru.kbe.beer.services.dto.events;

import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.BeerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jt on 2019-06-24.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerEvent {

    private BeerDto beerDto;
}
