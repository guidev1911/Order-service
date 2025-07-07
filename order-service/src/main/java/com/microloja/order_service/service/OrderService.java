package com.microloja.order_service.service;

import com.microloja.order_service.config.RabbitMQConfig;
import com.microloja.order_service.dto.OrderRequestDTO;
import com.microloja.order_service.dto.OrderResponseDTO;
import com.microloja.order_service.model.Order;
import com.microloja.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public OrderResponseDTO create(OrderRequestDTO dto) {
        Order order = new Order();
        order.setProductName(dto.getProduct());
        order.setQuantity(dto.getQuantity());
        order.setPrice(dto.getPrice());
        order.setCreatedAt(LocalDateTime.now());

        Order saved = repository.save(order);
        logger.info("Pedido salvo no banco: {}", saved);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, saved);
        logger.info("Evento enviado ao RabbitMQ: {}", saved);

        return toResponseDTO(saved);
    }

    public List<OrderResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO findById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        return toResponseDTO(order);
    }

    private OrderResponseDTO toResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setProduct(order.getProductName());
        dto.setQuantity(order.getQuantity());
        dto.setPrice(order.getPrice());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
