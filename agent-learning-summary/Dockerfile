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
COPY agent-learning-summary/pom.xml /build/agent-learning-summary/pom.xml
COPY agent-learning-summary/src /build/agent-learning-summary/src
RUN cd /build/agent-learning-summary && mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /build/agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
