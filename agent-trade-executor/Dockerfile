FROM maven:3.9-eclipse-temurin-21 AS builder

# Build shared-db
COPY shared-db/pom.xml /build/shared-db/pom.xml
COPY shared-db/src /build/shared-db/src
RUN cd /build/shared-db && mvn clean install -DskipTests -q

# Build shared-utils
COPY shared-utils/pom.xml /build/shared-utils/pom.xml
COPY shared-utils/src /build/shared-utils/src
RUN cd /build/shared-utils && mvn clean install -DskipTests -q

# Build agent
COPY agent-trade-executor/pom.xml /build/agent-trade-executor/pom.xml
COPY agent-trade-executor/src /build/agent-trade-executor/src
RUN cd /build/agent-trade-executor && mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /build/agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
