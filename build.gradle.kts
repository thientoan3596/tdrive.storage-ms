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
    implementation(libs.bundles.jjwt)
    implementation(libs.bundles.hibernate.jakarta.validator)
    implementation(libs.springdoc.webflux)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.jug)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(libs.bundles.r2dbc)
    compileOnly(libs.bundles.mapstruct.lombok.compile)
    annotationProcessor(libs.bundles.mapstruct.lombok.annotation.processor)
    implementation("com.github.thientoan3596:spring.uuid-bytes-converters:v0.1.0-SNAPSHOT")
    implementation("com.github.thientoan3596:spring.rest-common:v0.1.0-SNAPSHOT2")
    implementation("com.github.thientoan3596:validation.valid-enum:v1.0.0-SNAPSHOT")
}
tasks.bootJar {
    archiveFileName.set("storage-ms.jar")
}