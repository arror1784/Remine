package com.remine.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Serves files written by `memory`'s LocalDiskImageStorageAdapter back out over HTTP, at the
 * same `/uploads` path prefix baked into the URLs that adapter returns.
 */
@Configuration
class StaticUploadsConfig(
    @Value("\${storage.upload-dir:./uploads}") private val uploadDir: String,
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val directory = Paths.get(uploadDir).toAbsolutePath().normalize()
        // Path#toUri() only appends the trailing slash a directory resource location needs
        // when the directory already exists — it may not yet on a first boot, before anything
        // has been uploaded.
        Files.createDirectories(directory)
        registry.addResourceHandler("/uploads/**").addResourceLocations(directory.toUri().toString())
    }
}
