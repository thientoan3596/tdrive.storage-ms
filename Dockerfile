FROM amazoncorretto:17
RUN yum install -y curl && yum clean all
WORKDIR /app
COPY gradle ./gradle/
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x gradlew
RUN ./gradlew dependencies
COPY src ./src
RUN ./gradlew bootJar
CMD ["java", "-jar", "./build/libs/storage-ms.jar"]
