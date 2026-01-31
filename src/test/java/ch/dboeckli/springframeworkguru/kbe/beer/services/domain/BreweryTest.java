package ch.dboeckli.springframeworkguru.kbe.beer.services.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BreweryTest {

    @Test
    void isNew_returnsTrue_whenIdIsNull() {
        Brewery brewery = Brewery.builder()
            .id(null)
            .build();

        assertThat(brewery.isNew()).isTrue();
    }

    @Test
    void isNew_returnsFalse_whenIdIsSet() {
        Brewery brewery = Brewery.builder()
            .id(UUID.randomUUID())
            .build();

        assertThat(brewery.isNew()).isFalse();
    }
}
