package com.uees.studentservices.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    private final AppProperties props;

    public RabbitMQConfig(AppProperties props) {
        this.props = props;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter conv) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(conv);
        return t;
    }

    /* ---- Exchange compartido y DLX ---- */

    @Bean
    public TopicExchange enrollmentsExchange() {
        return ExchangeBuilder.topicExchange(props.getRabbit().getEnrollmentsExchange()).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(props.getRabbit().getDeadLetterExchange()).durable(true).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("enrollments.dlq").build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(props.getRabbit().getDeadLetterRoutingKey());
    }

    /* ---- enrollment.activated ---- */

    @Bean
    public Queue enrollmentActivatedQueue() {
        Map<String, Object> args = Map.of(
                "x-dead-letter-exchange", props.getRabbit().getDeadLetterExchange(),
                "x-dead-letter-routing-key", props.getRabbit().getDeadLetterRoutingKey()
        );
        return QueueBuilder.durable(props.getRabbit().getEnrollmentActivatedQueue())
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding enrollmentActivatedBinding(Queue enrollmentActivatedQueue, TopicExchange enrollmentsExchange) {
        return BindingBuilder.bind(enrollmentActivatedQueue)
                .to(enrollmentsExchange)
                .with(props.getRabbit().getEnrollmentActivatedRoutingKey());
    }

    /* ---- module.completed ---- */

    @Bean
    public Queue moduleCompletedQueue() {
        Map<String, Object> args = Map.of(
                "x-dead-letter-exchange", props.getRabbit().getDeadLetterExchange(),
                "x-dead-letter-routing-key", props.getRabbit().getDeadLetterRoutingKey()
        );
        return QueueBuilder.durable(props.getRabbit().getModuleCompletedQueue())
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding moduleCompletedBinding(Queue moduleCompletedQueue, TopicExchange enrollmentsExchange) {
        return BindingBuilder.bind(moduleCompletedQueue)
                .to(enrollmentsExchange)
                .with(props.getRabbit().getModuleCompletedRoutingKey());
    }
}
