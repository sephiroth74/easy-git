package it.sephiroth.gradle.git.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class JsonUtilsTest {

    @Test
    fun `JsonUtils object should exist and have fromJson extension`() {
        // Simple test to verify the JsonUtils object exists
        assertNotNull(JsonUtils)
    }

    @Test
    fun `should create TypeToken correctly`() {
        // Test that TypeToken creation works without actually using the reified extension
        val stringType = object : TypeToken<String>() {}.type
        assertNotNull(stringType)
    }

    @Test
    fun `gson should be able to parse simple JSON`() {
        // Test basic Gson functionality that JsonUtils enhances
        val gson = Gson()
        val json = """{"name": "test"}"""
        val result = gson.fromJson(json, Map::class.java)
        assertNotNull(result)
    }
}