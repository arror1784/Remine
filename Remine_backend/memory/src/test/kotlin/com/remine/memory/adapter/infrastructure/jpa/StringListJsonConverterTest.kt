package com.remine.memory.adapter.infrastructure.jpa

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringListJsonConverterTest {

    private val converter = StringListJsonConverter()

    @Test
    fun `convertToDatabaseColumn converts list to JSON string`() {
        val list = listOf("option1", "option2", "option3")
        val json = converter.convertToDatabaseColumn(list)
        assertEquals("""["option1","option2","option3"]""", json)
    }

    @Test
    fun `convertToDatabaseColumn handles null input`() {
        val json = converter.convertToDatabaseColumn(null)
        assertEquals("[]", json)
    }

    @Test
    fun `convertToEntityAttribute converts JSON string to list`() {
        val json = """["option1","option2","option3"]"""
        val list = converter.convertToEntityAttribute(json)
        assertEquals(listOf("option1", "option2", "option3"), list)
    }

    @Test
    fun `convertToEntityAttribute handles null or empty string`() {
        assertEquals(emptyList<String>(), converter.convertToEntityAttribute(null))
        assertEquals(emptyList<String>(), converter.convertToEntityAttribute(""))
        assertEquals(emptyList<String>(), converter.convertToEntityAttribute("   "))
    }
}
