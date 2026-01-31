package ch.dboeckli.springframeworkguru.kbe.beer.services.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BeerTest {

    @Test
    void isNew_returnsTrue_whenIdIsNull() {
        Beer beer = Beer.builder()
            .id(null)
            .build();

        assertThat(beer.isNew()).isTrue();
    }

    @Test
    void isNew_returnsFalse_whenIdIsSet() {
        Beer beer = Beer.builder()
            .id(UUID.randomUUID())
            .build();

        assertThat(beer.isNew()).isFalse();
    }
}
