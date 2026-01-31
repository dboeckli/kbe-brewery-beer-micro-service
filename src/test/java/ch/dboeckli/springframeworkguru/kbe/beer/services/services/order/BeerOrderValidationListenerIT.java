package ch.dboeckli.springframeworkguru.kbe.beer.services.services.order;

import ch.guru.springframework.kbe.lib.dto.BeerOrderDto;
import ch.guru.springframework.kbe.lib.events.BeerOrderValidationResult;
import ch.guru.springframework.kbe.lib.events.ValidateBeerOrderRequest;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
    "sfg.brewery.queues.validate-order-result=validate-order-result-test"
})
@Slf4j
class BeerOrderValidationListenerIT {

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BeerOrderValidationListener beerOrderValidationListener;

    @MockitoBean
    BeerOrderValidator beerOrderValidator;

    @Value("${sfg.brewery.queues.validate-order-result}")
    String validateOrderResultQueue;

    @Test
    void testValidationListenerSendsResult() {
        // ARRANGE
        BeerOrderDto order = BeerOrderDto.builder()
            .id(UUID.randomUUID())
            .build();

        given(beerOrderValidator.validateOrder(order)).willReturn(true);

        ValidateBeerOrderRequest request = new ValidateBeerOrderRequest(order);

        // ACT: Request senden
        beerOrderValidationListener.listen(request);

        // ASSERT: Result aus Result-Queue lesen
        BeerOrderValidationResult result = awaitResult(validateOrderResultQueue, order.getId());

        assertThat(result).isNotNull();
        assertThat(result.getBeerOrderId()).isEqualTo(order.getId());
        assertThat(result.getIsValid()).isTrue();
    }

    private BeerOrderValidationResult awaitResult(String queueName, UUID expectedOrderId) {
        AtomicReference<BeerOrderValidationResult> foundRef = new AtomicReference<>();
        jmsTemplate.setReceiveTimeout(100);

        try {
            Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    Message message = jmsTemplate.receive(queueName);
                    if (message instanceof TextMessage textMessage) {
                        try {
                            String payload = textMessage.getText();
                            BeerOrderValidationResult result =
                                objectMapper.readValue(payload, BeerOrderValidationResult.class);

                            if (expectedOrderId.equals(result.getBeerOrderId())) {
                                foundRef.set(result);
                                return true;
                            }
                        } catch (Exception e) {
                            log.warn("Konnte Ergebnis nicht deserialisieren: {}", e.getMessage());
                        }
                    }
                    return false;
                });
        } catch (Exception e) {
            return null;
        }

        return foundRef.get();
    }
}
