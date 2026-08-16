plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // spring-security-core only (not the starter): GlobalExceptionHandler maps
    // AccessDeniedException to 403, but common must not pull in security
    // auto-configuration.
    implementation("org.springframework.security:spring-security-core")
}

tasks.getByName("jar") {
    enabled = true
}
