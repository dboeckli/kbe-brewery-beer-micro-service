package ch.dboeckli.springframeworkguru.kbe.beer.services.services.order;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.repositories.BeerRepository;
import ch.guru.springframework.kbe.lib.dto.BeerOrderDto;
import ch.guru.springframework.kbe.lib.dto.BeerOrderLineDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

public class BeerOrderValidatorTest {

    private BeerRepository beerRepository;
    private BeerOrderValidator beerOrderValidator;

    @BeforeEach
    void setUp() {
        beerRepository = Mockito.mock(BeerRepository.class);
        beerOrderValidator = new BeerOrderValidator(beerRepository);
    }

    @Test
    void validateOrder_returnsTrue_whenAllUpcsExist() {
        // ARRANGE
        BeerOrderDto order = BeerOrderDto.builder()
            .beerOrderLines(List.of(
                BeerOrderLineDto.builder().upc("UPC-1").build(),
                BeerOrderLineDto.builder().upc("UPC-2").build()
            ))
            .build();

        given(beerRepository.findByUpc("UPC-1")).willReturn(Beer.builder().build());
        given(beerRepository.findByUpc("UPC-2")).willReturn(Beer.builder().build());

        // ACT
        Boolean valid = beerOrderValidator.validateOrder(order);

        // ASSERT
        assertThat(valid).isTrue();
    }

    @Test
    void validateOrder_returnsFalse_whenAnyUpcMissing() {
        // ARRANGE
        BeerOrderDto order = BeerOrderDto.builder()
            .beerOrderLines(List.of(
                BeerOrderLineDto.builder().upc("UPC-1").build(),
                BeerOrderLineDto.builder().upc("UPC-MISSING").build()
            ))
            .build();

        given(beerRepository.findByUpc("UPC-1")).willReturn(Beer.builder().build());
        given(beerRepository.findByUpc("UPC-MISSING")).willReturn(null);

        // ACT
        Boolean valid = beerOrderValidator.validateOrder(order);

        // ASSERT
        assertThat(valid).isFalse();
    }

}
