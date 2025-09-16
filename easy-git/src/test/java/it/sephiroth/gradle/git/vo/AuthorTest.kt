package it.sephiroth.gradle.git.vo

import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthorTest {

    @Test
    fun `should create Author with all fields`() {
        val author = Author(
            name = "John Doe",
            email = "john.doe@example.com",
            isoDate = "2023-10-15T10:30:00+02:00"
        )

        assertEquals("John Doe", author.name)
        assertEquals("john.doe@example.com", author.email)
        assertEquals("2023-10-15T10:30:00+02:00", author.isoDate)
    }

    @Test
    fun `should create Author with null email`() {
        val author = Author(
            name = "John Doe",
            email = null,
            isoDate = "2023-10-15T10:30:00+02:00"
        )

        assertEquals("John Doe", author.name)
        assertNull(author.email)
        assertEquals("2023-10-15T10:30:00+02:00", author.isoDate)
    }

    @Test
    fun `should create Author with null date`() {
        val author = Author(
            name = "John Doe",
            email = "john.doe@example.com",
            isoDate = null
        )

        assertEquals("John Doe", author.name)
        assertEquals("john.doe@example.com", author.email)
        assertNull(author.isoDate)
        assertNull(author.dateTime)
    }

    @Test
    fun `should parse valid ISO date time`() {
        val author = Author(
            name = "John Doe",
            email = "john.doe@example.com",
            isoDate = "2023-10-15T10:30:00+02:00"
        )

        val expectedDateTime = OffsetDateTime.of(2023, 10, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2))
        assertEquals(expectedDateTime, author.dateTime)
    }

    @Test
    fun `should parse UTC ISO date time`() {
        val author = Author(
            name = "John Doe",
            email = "john.doe@example.com",
            isoDate = "2023-10-15T10:30:00Z"
        )

        val expectedDateTime = OffsetDateTime.of(2023, 10, 15, 10, 30, 0, 0, ZoneOffset.UTC)
        assertEquals(expectedDateTime, author.dateTime)
    }

    @Test
    fun `should return null dateTime for null isoDate`() {
        val author = Author(
            name = "John Doe",
            email = "john.doe@example.com",
            isoDate = null
        )

        assertNull(author.dateTime)
    }

    @Test
    fun `should have proper toString format`() {
        val author = Author(
            name = "John Doe",
            email = "john.doe@example.com",
            isoDate = "2023-10-15T10:30:00+02:00"
        )

        val expected = "Author(name='John Doe', email=john.doe@example.com, dateTime=2023-10-15T10:30:00+02:00)"
        assertEquals(expected, author.toString())
    }

    @Test
    fun `should have proper toString format with null email`() {
        val author = Author(
            name = "John Doe",
            email = null,
            isoDate = "2023-10-15T10:30:00+02:00"
        )

        val expected = "Author(name='John Doe', email=null, dateTime=2023-10-15T10:30:00+02:00)"
        assertEquals(expected, author.toString())
    }

    @Test
    fun `data class should have proper equality`() {
        val author1 = Author("John Doe", "john@example.com", "2023-10-15T10:30:00+02:00")
        val author2 = Author("John Doe", "john@example.com", "2023-10-15T10:30:00+02:00")
        val author3 = Author("Jane Doe", "jane@example.com", "2023-10-15T10:30:00+02:00")

        assertEquals(author1, author2)
        assertEquals(author1.hashCode(), author2.hashCode())
        assert(author1 != author3)
    }
}