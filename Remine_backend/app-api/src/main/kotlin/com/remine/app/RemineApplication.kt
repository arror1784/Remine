package com.remine.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// Every module lives under its own com.remine.<module> package rather than a
// shared subtree of com.remine.app, so the scan bases must be declared
// explicitly — Spring Boot's default component/entity/repository scanning
// only looks under this class's own package by default.
@SpringBootApplication(scanBasePackages = ["com.remine"])
@EnableJpaRepositories(basePackages = ["com.remine"])
@EntityScan(basePackages = ["com.remine"])
class RemineApplication

fun main(args: Array<String>) {
    runApplication<RemineApplication>(*args)
}
