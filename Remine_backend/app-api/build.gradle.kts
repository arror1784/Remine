plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    application
}

application {
    mainClass.set("com.remine.app.RemineApplicationKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":client-openai"))
    implementation(project(":migration"))
    implementation(project(":auth"))
    implementation(project(":user"))
    implementation(project(":notification"))
    implementation(project(":message"))
    implementation(project(":call"))
    implementation(project(":memory"))
    implementation(project(":activity"))
    implementation(project(":family"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.integration:spring-integration-redis")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springdoc:springdoc-openapi-ui:1.7.0")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
}
