/*
 * This file was generated by the Gradle "init" task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.10.2/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.springframework.boot") version "3.4.1" // Replace with your desired version
    id("io.spring.dependency-management") version "1.1.6" // Dependency management plugin
}
ext {
    set("springCloudVersion", "2024.0.0")
}

dependencyManagement {
    imports {
        mavenBom ("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
    }
}
repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    mavenLocal()
}


dependencies {
    // Use JUnit Jupiter for testing.
   // testImplementation(junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-all:3.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation ("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("io.projectreactor:reactor-test")
    testImplementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    testImplementation("com.h2database:h2:2.2.220")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.0.1")
    testImplementation("org.springframework.security:spring-security-test")
    implementation("me.sonam:webclients:1.0.0-SNAPSHOT")

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "cloud.sonam.friendships.Application"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
