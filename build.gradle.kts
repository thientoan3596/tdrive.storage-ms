plugins {
    id("java")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}
group = "org.thluon.java"
version = "1.0-SNAPSHOT"
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}
dependencies {
    implementation(platform(libs.spring.cloud.dependencies))
    implementation(libs.bundles.jjwt)
    implementation(libs.bundles.hibernate.jakarta.validator)
    implementation(libs.springdoc.webflux)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.jug)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation(libs.bundles.r2dbc)
    implementation(libs.thluon.converter)
    implementation(libs.thluon.rest)
    compileOnly(libs.bundles.mapstruct.lombok.compile)
    annotationProcessor(libs.bundles.mapstruct.lombok.annotation.processor)
    implementation("com.github.thientoan3596:spring.uuid-bytes-converters:v0.1.0-SNAPSHOT")
    implementation("com.github.thientoan3596:spring.rest-common:v0.1.0-SNAPSHOT2")
    implementation("com.github.thientoan3596:validation.valid-enum:v1.0.0-SNAPSHOT")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("com.google.jimfs:jimfs:1.1")
    testImplementation("io.projectreactor:reactor-test:3.7.5")
    constraints {
        implementation("com.thoughtworks.xstream:xstream:1.4.21")
        implementation("commons-io:commons-io:2.14.0")
        implementation("org.apache.httpcomponents:httpclient:4.5.13")
    }
}
tasks.bootJar {
    archiveFileName.set("storage-ms.jar")
}
tasks.bootRun {
    doFirst {
        file(".env").readLines().forEach {
            val cleanLine = it.trim().split("#")[0].trim()
            if (cleanLine.isNotEmpty()) {
                val parts = cleanLine.split("=", limit = 2)
                if (parts.size == 2 && parts[0].isNotEmpty()) {
                    val (key, value) = parts
                    environment(key, value)
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}