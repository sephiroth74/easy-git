package it.sephiroth.gradle.git.lib

import java.util.concurrent.Callable

abstract class GitCommand<T>(protected val repo: Repository) : Callable<T> {
    abstract override fun call(): T

    internal class ParamsBuilder {
        private val array = mutableListOf<String>()

        fun addAll(vararg params: GitParam) {
            params.forEach { this.add(it) }
        }

        fun add(param: GitParam?) {
            param?.asQueryString()?.let { array.add(it) }
        }

        fun add(value: String) = array.add(value)

        fun add(value: String?) = value?.let { add(it) }

        override fun toString(): String {
            return array.joinToString(" ")
        }

        fun toList(): List<String> = array.toList()
    }

    internal class GitNameParam(name: String) : GitNullableParam(name) {
        override fun asQueryString(): String? {
            return if (isPreset) name else null
        }
    }

    internal class GitNameValueParam<T>(name: String, var value: T? = null) : GitNullableParam(name) {
        init {
            isPreset = value != null
        }

        override fun set() = set(null)

        fun set(newValue: T?): GitNameValueParam<T> {
            value = newValue
            isPreset = true
            return this
        }

        override fun unset(): GitNameValueParam<T> {
            isPreset = false
            value = null
            return this
        }

        override fun asQueryString(): String? {
            return if (isPreset) {
                val list: List<String> = value?.let { queryValue ->
                    when (queryValue) {
                        is GitParam -> fromGitParam(name, queryValue)
                        else -> listOf(name, value.toString())
                    }
                } ?: run { listOf(name) }
                list.joinToString(if (name.startsWith("--")) "=" else " ")
            } else null
        }

        private fun fromGitParam(name: String, queryValue: GitParam): List<String> {
            return queryValue.asQueryString()?.let { value ->
                listOf(name, value)
            } ?: run {
                listOf(name)
            }
        }
    }

    /**
     * to be used for arguments like [-n[<num>]] where the argument and its value become
     * the argument itself
     */
    internal class GitNumParam<T>(name: String, var value: T? = null) : GitNullableParam(name) {
        init {
            isPreset = value != null
        }

        override fun set() = set(null)

        fun set(newValue: T?): GitNumParam<T> {
            value = newValue
            isPreset = true
            return this
        }

        override fun unset(): GitNumParam<T> {
            isPreset = false
            value = null
            return this
        }

        override fun asQueryString(): String? {
            return if (isPreset) {
                value?.let { queryValue ->
                    when (queryValue) {
                        is GitParam -> fromGitParam(name, queryValue)
                        else -> "$name$value"
                    }
                } ?: run {
                    name
                }
            } else null
        }

        private fun fromGitParam(name: String, queryValue: GitParam): String? {
            return queryValue.asQueryString()?.let { value -> "$name$value" }
        }
    }

    abstract class GitNullableParam(val name: String) : GitParam {
        var isPreset: Boolean = false
            protected set

        open fun set() = apply { isPreset = true }
        open fun unset() = apply { isPreset = false }
    }

    interface GitParam {
        fun asQueryString(): String?
    }

}
