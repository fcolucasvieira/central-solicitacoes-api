FROM eclipse-temurin:25_36-jdk-noble AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25_36-jre-noble

WORKDIR /app

RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
