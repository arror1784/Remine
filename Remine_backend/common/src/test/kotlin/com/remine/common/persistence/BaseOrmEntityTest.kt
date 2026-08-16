package com.remine.common.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class BaseOrmEntityTest {

    private class TestEntity(id: UUID = UUID.randomUUID()) : BaseOrmEntity(id)

    @Test
    fun `new entity is not deleted`() {
        val entity = TestEntity()

        assertFalse(entity.isDeleted)
        assertNull(entity.deletedAt)
    }

    @Test
    fun `softDelete marks the entity deleted with a timestamp`() {
        val entity = TestEntity()

        entity.softDelete()

        assertTrue(entity.isDeleted)
        assertNotNull(entity.deletedAt)
    }

    @Test
    fun `entities are equal when ids match regardless of other state`() {
        val id = UUID.randomUUID()
        val entityA = TestEntity(id)
        val entityB = TestEntity(id)
        entityB.softDelete()

        assertEquals(entityA, entityB)
        assertEquals(entityA.hashCode(), entityB.hashCode())
    }

    @Test
    fun `entities with different ids are not equal`() {
        val entityA = TestEntity()
        val entityB = TestEntity()

        assertNotEquals(entityA, entityB)
    }
}
