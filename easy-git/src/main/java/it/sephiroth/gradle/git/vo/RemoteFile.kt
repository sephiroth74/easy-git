package it.sephiroth.gradle.git.vo

import java.util.*

data class RemoteFile(val type: Type) {
    var tree: String? = null
    var parent: String? = null
    var author: Author? = null
    var committer: Author? = null

    enum class Type {
        Commit, Tag;

        companion object {
            fun of(value: String): Type {
                return values().first { it.name.toLowerCase(Locale.ROOT) == value }
            }
        }
    }
}
