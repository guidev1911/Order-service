package com.microloja.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microloja.order_service.dto.OrderRequestDTO;
import com.microloja.order_service.dto.OrderResponseDTO;
import com.microloja.order_service.model.Order;
import com.microloja.order_service.repository.OrderRepository;
import com.microloja.order_service.service.OrderPublisher;
import com.microloja.order_service.service.OrderService;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void setup() {
    }
    @TestConfiguration
    static class TestConfig {

        @Bean
        public OrderPublisher orderPublisher() {
            return Mockito.mock(OrderPublisher.class);
        }

        @Bean
        public OrderService orderService(OrderRepository repository, OrderPublisher orderPublisher) {
            return Mockito.spy(new OrderService(repository, orderPublisher));
        }
    }

    @Test
    void deveCriarPedidoComSucesso() throws Exception {
        OrderRequestDTO dto = new OrderRequestDTO("Notebook", 1, new BigDecimal("3000.00"));

        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setProduct("Notebook");
        responseDTO.setQuantity(1);
        responseDTO.setPrice(new BigDecimal("3000.00"));
        responseDTO.setCreatedAt(LocalDateTime.now());

        doReturn(responseDTO).when(orderService).create(any(OrderRequestDTO.class));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.product").value("Notebook"))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void deveRetornarTodosOsPedidos() throws Exception {
        repository.deleteAll();

        Order order = new Order(null, "Teclado", 2, new BigDecimal("100.00"), LocalDateTime.now());
        repository.save(order);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].product", Matchers.hasItem("Teclado")));
    }

    @Test
    void deveRetornarPedidoPorId() throws Exception {
        Order order = new Order(null, "Mouse", 1, new BigDecimal("50.00"), LocalDateTime.now());
        Order saved = repository.save(order);

        mockMvc.perform(get("/api/orders/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("Mouse"));
    }
}