package ch.dboeckli.springframeworkguru.kbe.beer.services.web.mappers;

import ch.dboeckli.springframeworkguru.kbe.beer.services.domain.Beer;
import ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory.BeerInventoryService;
import ch.guru.springframework.kbe.lib.dto.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

class BeerMapperDecoratorTest {

    private BeerInventoryService beerInventoryService;
    private BeerMapper delegateMapper;
    private BeerMapperDecorator decorator;

    @BeforeEach
    void setUp() {
        beerInventoryService = Mockito.mock(BeerInventoryService.class);
        delegateMapper = Mockito.mock(BeerMapper.class);

        decorator = new BeerMapperDecorator() {
            private BeerMapper delegate;

            @Override
            public void setMapper(BeerMapper mapper) {
                super.setMapper(mapper);
                this.delegate = mapper;
            }

            @Override
            public BeerDto beerToBeerDto(Beer beer) {
                return delegate.beerToBeerDto(beer);
            }

            @Override
            public Beer beerDtoToBeer(BeerDto beerDto) {
                return delegate.beerDtoToBeer(beerDto);
            }
        };
        decorator.setBeerInventoryService(beerInventoryService);
        decorator.setMapper(delegateMapper);
    }

    @Test
    void beerToBeerDtoWithInventory_setsQuantityOnHandFromInventoryService() {
        // ARRANGE
        UUID beerId = UUID.randomUUID();

        Beer beer = Beer.builder()
            .id(beerId)
            .build();

        BeerDto dto = BeerDto.builder()
            .id(beerId)
            .build();

        given(delegateMapper.beerToBeerDto(beer)).willReturn(dto);
        given(beerInventoryService.getOnhandInventory(beerId)).willReturn(42);

        // ACT
        BeerDto result = decorator.beerToBeerDtoWithInventory(beer);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(beerId);
        assertThat(result.getQuantityOnHand()).isEqualTo(42);
    }

}