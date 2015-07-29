package fr.cgi.tps.atp.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * Message -> Channel -> QueueChannel
 * 
 * @author hvle
 *
 */
@Slf4j
@SpringBootApplication
public class HelloOneOhOneApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(HelloOneOhOneApplication.class, args);

        DirectChannel inputChannel = context.getBean("inputChannel", DirectChannel.class);
        inputChannel.send(new GenericMessage<String>("World"));

        QueueChannel outputChannel = context.getBean("outputChannel", QueueChannel.class);
        log.info("==> {}", outputChannel.receive().getPayload());
    }

    @Bean
    public IntegrationFlow orders() {

        return
        // direct channel named inputChannel
        IntegrationFlows.from("inputChannel")

        // ouput queue channel attached to our service activator
                .channel(MessageChannels.queue("outputChannel", 10))

        .get();
    }
}
