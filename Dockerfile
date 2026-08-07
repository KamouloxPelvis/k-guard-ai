FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080
ENV KGUARD_AI_LLM_ENABLED=true
ENV KGUARD_AI_LLM_PROVIDER=ollama
ENV KGUARD_AI_LLM_BASE_URL=http://ollama:11434
ENV KGUARD_AI_LLM_MODEL=qwen3:0.6b
ENV KGUARD_AI_LLM_TIMEOUT_SECONDS=30

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
