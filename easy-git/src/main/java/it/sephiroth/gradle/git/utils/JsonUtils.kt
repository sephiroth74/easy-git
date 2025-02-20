package it.sephiroth.gradle.git.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonUtils {
    inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object : TypeToken<T>() {}.type)
}
