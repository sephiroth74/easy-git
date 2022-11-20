package it.sephiroth.gradle.git.vo

class LogCommit {
    val values: MutableMap<CommitLineType, String?> = mutableMapOf()

    fun get(type: CommitLineType): String? = values[type]

    fun set(key: CommitLineType, value: String?) {
        values[key] = value
    }

    fun entries() = values.entries

    fun load(type: String, text: String) = set(CommitLineType.of(type), text)
    override fun toString(): String {
        return "LogCommit(${values.map { "${it.key.value}: ${it.value}" }.joinToString(", ")})"
    }

    enum class CommitLineType(val value: String, val format: String) {
        Commit("commit", "%H"),
        Tree("tree", "%T"),
        Parent("parent", "%P"),
        Subject("subject", "%s"),
        Author("author", "%an"),
        Email("email", "%aE"),
        Date("date", "%ai"),
        Tags("tags", "%D");

        companion object {
            fun of(string: String): CommitLineType {
                return values().first { it.value == string }
            }
        }
    }
}
