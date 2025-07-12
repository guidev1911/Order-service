package com.microloja.order_service.service;

import com.microloja.order_service.config.RabbitMQConfig;
import com.microloja.order_service.dto.OrderRequestDTO;
import com.microloja.order_service.dto.OrderResponseDTO;

import com.microloja.order_service.exceptions.OrderNotFoundException;
import com.microloja.order_service.model.Order;
import com.microloja.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private OrderPublisher orderPublisher;

    @InjectMocks
    private OrderService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarPedidoEEviarParaORabbitMQ() {
        OrderRequestDTO dto = new OrderRequestDTO("Produto A", 3, new BigDecimal("29.99"));
        Order savedOrder = new Order(1L, "Produto A", 3, new BigDecimal("29.99"), LocalDateTime.now());

        when(repository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDTO response = service.create(dto);

        assertThat(response).isNotNull();
        assertThat(response.getProduct()).isEqualTo("Produto A");
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getPrice()).isEqualByComparingTo("29.99");

        verify(repository, times(1)).save(any(Order.class));
        verify(orderPublisher, times(1)).send(argThat(event ->
                event.getOrderId().equals("1") &&
                        event.getProduct().equals("Produto A") &&
                        event.getQuantity().equals(3) &&
                        event.getTotal().compareTo(new BigDecimal("29.99")) == 0
        ));
    }

    @Test
    void deveBuscarTodosOsPedidos() {
        List<Order> orders = List.of(
                new Order(1L, "Produto A", 2, new BigDecimal("10.00"), LocalDateTime.now()),
                new Order(2L, "Produto B", 5, new BigDecimal("15.00"), LocalDateTime.now())
        );

        when(repository.findAll()).thenReturn(orders);

        List<OrderResponseDTO> result = service.findAll();

        assertThat(result).hasSize(2);
        verify(repository, times(1)).findAll();
    }

    @Test
    void deveBuscarPedidoPorId() {
        Order order = new Order(1L, "Produto X", 1, new BigDecimal("50.00"), LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDTO response = service.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProduct()).isEqualTo("Produto X");
    }

    @Test
    void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Pedido não encontrado");
    }
}

