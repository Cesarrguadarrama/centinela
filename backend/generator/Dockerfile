# ---- Build stage -------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy POMs first so dependency resolution is cached between builds
COPY pom.xml .
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY bootstrap/pom.xml bootstrap/
COPY generator/pom.xml generator/
RUN mvn -B -ntp -q dependency:go-offline -pl generator -am

COPY domain/src domain/src
COPY generator/src generator/src
RUN mvn -B -ntp package -pl generator -am -DskipTests

# ---- Runtime stage ------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S centinela && adduser -S centinela -G centinela
USER centinela
WORKDIR /app
COPY --from=build /workspace/generator/target/centinela-generator-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
