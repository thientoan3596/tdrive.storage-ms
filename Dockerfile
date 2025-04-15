FROM amazoncorretto:17
WORKDIR /app
COPY src ./src
COPY gradle ./gradle/
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon
EXPOSE 8761
CMD ["java", "-jar", "./build/libs/storage-ms.jar"]
