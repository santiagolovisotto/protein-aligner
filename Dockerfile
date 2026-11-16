# Etapa 1: compilar con Gradle
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /app

# Primero solo los archivos de build, para aprovechar la cache de capas
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
# El sed evita el error "bad interpreter" si gradlew llego con finales de linea de Windows
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew \
 && ./gradlew --no-daemon dependencies > /dev/null

COPY src src
RUN ./gradlew --no-daemon bootJar -x test \
 && find build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /app/app.jar \;

# Etapa 2: ejecutar solo el jar, sin Gradle ni JDK completo
FROM eclipse-temurin:21-jre-noble
WORKDIR /app

RUN useradd --system --create-home appuser
COPY --from=build /app/app.jar app.jar
USER appuser

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]