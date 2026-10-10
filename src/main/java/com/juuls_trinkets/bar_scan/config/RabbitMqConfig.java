package com.juuls_trinkets.bar_scan.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange productCreatedExchange() {
        return new DirectExchange(RabbitMqTopology.PRODUCT_CREATED_EXCHANGE, true, false);
    }

    @Bean
    public Queue productCreatedQueue() {
        return new Queue(RabbitMqTopology.PRODUCT_CREATED_QUEUE, true);
    }

    @Bean
    public Binding productCreatedBinding(
            Queue productCreatedQueue,
            DirectExchange productCreatedExchange
    ) {
        return BindingBuilder.bind(productCreatedQueue)
                .to(productCreatedExchange)
                .with(RabbitMqTopology.PRODUCT_CREATED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange syncBatchExchange() {
        return new DirectExchange(RabbitMqTopology.SYNC_BATCH_EXCHANGE, true, false);
    }

    @Bean
    public Queue syncBatchStartedQueue() {
        return new Queue(RabbitMqTopology.SYNC_BATCH_STARTED_QUEUE, true);
    }

    @Bean
    public Queue syncBatchPublishCompletedQueue() {
        return new Queue(RabbitMqTopology.SYNC_BATCH_PUBLISH_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding syncBatchStartedBinding(
            Queue syncBatchStartedQueue,
            DirectExchange syncBatchExchange
    ) {
        return BindingBuilder.bind(syncBatchStartedQueue)
                .to(syncBatchExchange)
                .with(RabbitMqTopology.SYNC_BATCH_STARTED_ROUTING_KEY);
    }

    @Bean
    public Binding syncBatchPublishCompletedBinding(
            Queue syncBatchPublishCompletedQueue,
            DirectExchange syncBatchExchange
    ) {
        return BindingBuilder.bind(syncBatchPublishCompletedQueue)
                .to(syncBatchExchange)
                .with(RabbitMqTopology.SYNC_BATCH_PUBLISH_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange signupNotificationExchange() {
        return new DirectExchange(RabbitMqTopology.SIGNUP_NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue signupNotificationQueue() {
        return new Queue(RabbitMqTopology.SIGNUP_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding signupNotificationBinding(
            Queue signupNotificationQueue,
            DirectExchange signupNotificationExchange
    ) {
        return BindingBuilder.bind(signupNotificationQueue)
                .to(signupNotificationExchange)
                .with(RabbitMqTopology.SIGNUP_NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAutoStartup(autoStartup);
        return factory;
    }
}
