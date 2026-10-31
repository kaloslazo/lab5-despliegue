FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
# ffmpeg para el video y fuentes para dibujar el texto sobre las fotos
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
RUN useradd -r -u 1001 spring && mkdir -p /data/media && chown -R spring /data
COPY --from=build /src/app.jar app.jar
USER spring
ENV MEDIA_DIR=/data/media
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
