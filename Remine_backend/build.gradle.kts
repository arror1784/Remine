import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.18" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
    kotlin("plugin.jpa") version "1.9.22" apply false
}

allprojects {
    group = "com.remine"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")

    // Spring Boot 2.7's BOM pins kotlin-stdlib/kotlin-reflect to an old
    // version (1.6.21) for its own compatibility baseline. Without this
    // override, that BOM-managed version silently wins over the 1.9.22
    // declared above for every configuration on the module — including the
    // Kotlin compiler's own daemon classpath, which then crashes with
    // NoClassDefFoundError: kotlin/enums/EnumEntriesKt (a class that only
    // exists from kotlin-stdlib 1.8.20+). Setting this extra property BEFORE
    // the BOM import is the standard way (matches what Spring Initializr
    // generates) to make the dependency-management plugin use our version
    // instead of the BOM's default.
    extra["kotlin.version"] = "1.9.22"

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.18")
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }
}
