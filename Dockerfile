FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/order-service-0.0.1-SNAPSHOT.jar /app/order-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "order-service.jar"]
