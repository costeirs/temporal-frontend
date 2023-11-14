plugins {
    java
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("io.freefair.lombok") version "8.4"
}

group = "com.kinandcarta.cjug"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // BOMs
    implementation(platform("io.temporal:temporal-bom:1.22.3"))
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.2.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // Temporal
    implementation("io.temporal:temporal-spring-boot-starter-alpha")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
}
