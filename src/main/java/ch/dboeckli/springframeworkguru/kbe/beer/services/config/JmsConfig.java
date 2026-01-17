package ch.dboeckli.springframeworkguru.kbe.beer.services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JmsConfig {

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter(JsonMapper objectMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
