# Order Service

Microserviço responsável pelo processamento de pedidos.

## O que ele faz?

- Recebe pedidos via REST.
- Persiste no banco de dados.
- Publica eventos de pedidos criados na fila RabbitMQ.
- Registrado no Eureka.
- Usa RabbitMQ para comunicação assíncrona com payment-service.

## Tecnologias usadas

- Spring Boot
- Spring Data JPA com PostgreSQL
- RabbitMQ
- Eureka Client
- Docker
- Swagger

## Endpoints principais

- POST /api/orders
- GET /api/orders
- GET /api/orders/{id}

