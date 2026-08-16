package com.remine.memory.adapter.infrastructure.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class StringListJsonConverter : AttributeConverter<List<String>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        if (attribute == null) return "[]"
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        if (dbData.isNullOrBlank()) return emptyList()
        return try {
            objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
        } catch (e: Exception) {
            emptyList()
        }
    }
}
