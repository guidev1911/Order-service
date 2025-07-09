package com.microloja.order_service.service;

import com.microloja.order_service.dto.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "order.exchange";
    private static final String ROUTING_KEY = "order.created";

    public void send(OrderEvent event) {
        logger.info("Enviando pedido para fila RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }

    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}