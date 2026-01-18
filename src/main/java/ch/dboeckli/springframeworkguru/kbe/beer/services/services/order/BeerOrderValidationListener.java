package ch.dboeckli.springframeworkguru.kbe.beer.services.services.order;

import ch.guru.springframework.kbe.lib.events.BeerOrderValidationResult;
import ch.guru.springframework.kbe.lib.events.ValidateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final BeerOrderValidator beerOrderValidator;
    private final JmsTemplate jmsTemplate;

    @Value("${sfg.brewery.queues.validate-order-result}")
    String validateOrderResultQueue;

    @JmsListener(destination = "${sfg.brewery.queues.validate-order}")
    public void listen(ValidateBeerOrderRequest event) {

        Boolean orderIsValid = beerOrderValidator.validateOrder(event.getBeerOrder());

        log.info("Validation Result for Order Id: " + event.getBeerOrder() + " is: " + orderIsValid);

        jmsTemplate.convertAndSend(validateOrderResultQueue, BeerOrderValidationResult.builder()
            .beerOrderId(event.getBeerOrder().getId())
            .isValid(orderIsValid)
            .build());
    }
}
