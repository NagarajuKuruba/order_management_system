package com.spry.oms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue}")
    private String queue;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public Queue queue() {
        log.info("Creating RabbitMQ Queue: {}", queue);
        Queue q = new Queue(queue);
        log.debug("Queue created successfully");
        return q;
    }

    @Bean
    public TopicExchange exchange() {
        log.info("Creating RabbitMQ TopicExchange: {}", exchange);
        TopicExchange ex = new TopicExchange(exchange);
        log.debug("TopicExchange created successfully");
        return ex;
    }

    @Bean
    public Binding binding() {
        log.info("Creating RabbitMQ Binding - Queue: {}, Exchange: {}, RoutingKey: {}", 
                queue, exchange, routingKey);
        Binding binding = BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingKey);
        log.debug("Binding created successfully");
        return binding;
    }
}