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
COPY agent-market-analyst/pom.xml /build/agent-market-analyst/pom.xml
COPY agent-market-analyst/src /build/agent-market-analyst/src
RUN cd /build/agent-market-analyst && mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /build/agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
