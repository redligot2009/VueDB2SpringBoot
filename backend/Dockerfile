# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml .
RUN ./mvnw -q -B -e -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


