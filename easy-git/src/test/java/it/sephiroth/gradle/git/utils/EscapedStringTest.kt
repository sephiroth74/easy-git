package it.sephiroth.gradle.git.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EscapedStringTest {

    @Test
    fun `should escape non-null string with quotes`() {
        val escapedString = EscapedString("hello world")

        assertEquals("\"hello world\"", escapedString.asQueryString())
    }

    @Test
    fun `should escape string with special characters`() {
        val escapedString = EscapedString("hello-world_123")

        assertEquals("\"hello-world_123\"", escapedString.asQueryString())
    }

    @Test
    fun `should escape empty string with quotes`() {
        val escapedString = EscapedString("")

        assertEquals("\"\"", escapedString.asQueryString())
    }

    @Test
    fun `should return null for null input`() {
        val escapedString = EscapedString(null)

        assertNull(escapedString.asQueryString())
    }

    @Test
    fun `should handle strings with existing quotes`() {
        val escapedString = EscapedString("hello \"world\"")

        assertEquals("\"hello \"world\"\"", escapedString.asQueryString())
    }

    @Test
    fun `should handle strings with spaces`() {
        val escapedString = EscapedString("commit message with spaces")

        assertEquals("\"commit message with spaces\"", escapedString.asQueryString())
    }

    @Test
    fun `data class should have proper equality`() {
        val escaped1 = EscapedString("test")
        val escaped2 = EscapedString("test")
        val escaped3 = EscapedString("different")

        assertEquals(escaped1, escaped2)
        assertEquals(escaped1.hashCode(), escaped2.hashCode())
        assert(escaped1 != escaped3)
    }
}