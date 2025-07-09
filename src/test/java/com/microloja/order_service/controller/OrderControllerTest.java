package com.microloja.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microloja.order_service.dto.OrderRequestDTO;
import com.microloja.order_service.model.Order;
import com.microloja.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Test
    void deveCriarPedidoComSucesso() throws Exception {

        OrderRequestDTO dto = new OrderRequestDTO("Notebook", 1, new BigDecimal("3000.00"));

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
                .andExpect(jsonPath("$[*].product").value(org.hamcrest.Matchers.hasItem("Teclado")));
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
