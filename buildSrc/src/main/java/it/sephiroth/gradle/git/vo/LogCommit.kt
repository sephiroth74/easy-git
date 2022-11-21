package it.sephiroth.gradle.git.vo

class LogCommit {
    val values: MutableMap<CommitLineType, String?> = mutableMapOf()
    val tags: List<String> = mutableListOf()

    val commitId: String? get() = values[CommitLineType.Commit]
    val tree: String? get() = values[CommitLineType.Tree]
    val parent: String? get() = values[CommitLineType.Parent]
    val subject: String? get() = values[CommitLineType.Subject]
    val body: String? get() = values[CommitLineType.Body]

    val sanitizedSubject: String?
        get() = subject?.replace(Regex("[\\W]+"), "_")

    val author: Author?
        get() {
            return values[CommitLineType.Author]?.let { authorName ->
                Author(
                    authorName,
                    values[CommitLineType.Email],
                    values[CommitLineType.Date]
                )
            }
        }


    internal fun set(key: CommitLineType, text: String?) {
        when (key) {
            CommitLineType.Tags -> {
                (tags as MutableList).clear()
                text?.trimEnd()?.split(",")?.let { splittet ->
                    tags.addAll(splittet)
                }
            }
            else -> values[key] = text?.trimEnd()
        }
    }

    internal fun entries() = values.entries

    internal fun load(type: String, text: String) {
        val key = CommitLineType.of(type)
        set(key, text)
    }

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
        Date("date", "%aI"),
        Body("body", "%B"),
        Tags("tags", "%D");

        companion object {
            fun of(string: String): CommitLineType {
                return values().first { it.value == string }
            }
        }
    }
}
