plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

tasks.getByName("jar") {
    enabled = true
}
