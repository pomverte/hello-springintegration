package fr.cgi.tps.atp.hello;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.support.GenericHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Message -> Gateway -> Channel -> ServiceActivator -> QueueChannel
 * 
 * @author hvle
 *
 */
@Slf4j
@SpringBootApplication
@IntegrationComponentScan
public class HelloGatewayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(HelloGatewayApplication.class, args);

        Inbox inbox = context.getBean(Inbox.class);
        inbox.placeMail("Away");

        QueueChannel outputChannel = context.getBean("outputChannel", QueueChannel.class);
        log.info("==> {} !", outputChannel.receive().getPayload());
    }

    @MessagingGateway
    public interface Inbox {
        // this is the way in !
        @Gateway(requestChannel = "inputChannel")
        void placeMail(String message);
    }

    @Bean
    public GenericHandler<Object> helloService() {
        return new GenericHandler<Object>() {
            @Override
            public Object handle(Object payload, Map<String, Object> headers) {
                return "I want to Fly " + payload;
            }
        };
    }

    @Bean
    public IntegrationFlow orders() {

        return
        // direct channel named inputChannel
        IntegrationFlows.from("inputChannel")

        // service activator with a handler
                .handle(this.helloService())

        // ouput queue channel attached to our service activator
                .channel(MessageChannels.queue("outputChannel", 10))

        .get();
    }
}
