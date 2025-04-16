FROM amazoncorretto:17
WORKDIR /app
COPY gradle ./gradle/
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x gradlew
RUN ./gradlew dependencies
COPY src ./src
RUN ./gradlew bootJar
RUN apt-get update && apt-get install -y curl
CMD ["java", "-jar", "./build/libs/storage-ms.jar"]
