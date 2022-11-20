package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import java.time.OffsetDateTime
import java.util.regex.Pattern

/**
 * @see <a href="">git log</a>
 */
class GitLogCommand(repo: Repository) : GitCommand<Iterable<Commit>>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val maxCount = GitNameValueParam<Int>("--max-count")
    private val skip = GitNameValueParam<Int>("--skip")
    private val since = GitNameValueParam<OffsetDateTime>("--since")
    private val until = GitNameValueParam<OffsetDateTime>("--until")
    private val author = GitNameValueParam<String>("--author")
    private val commmitter = GitNameValueParam<String>("--committer")

    private val merges = GitNameParam("--merges")
    private val noMerges = GitNameParam("--no-merges")
    private val reflog = GitNameParam("--reflog")
    private val firstParent = GitNameParam("--first-parent")

    private val minParents = GitNameValueParam<Int>("--min-parents")
    private val maxParents = GitNameValueParam<Int>("--max-parents")

    private var revisionRange = RevisionRangeParam.head()

    // endregion git arguments

    fun firstParent() = apply { firstParent.set() }
    fun reflog() = apply { reflog.set() }
    fun maxCount(value: Int) = apply { maxCount.set(value) }
    fun skip(value: Int) = apply { skip.set(value) }
    fun since(value: OffsetDateTime) = apply { since.set(value) }
    fun until(value: OffsetDateTime) = apply { until.set(value) }
    fun author(value: String) = apply { author.set(value) }
    fun committer(value: String) = apply { commmitter.set(value) }

    fun minParents(value: Int) = apply { minParents.set(value); merges.unset() }
    fun maxParents(value: Int) = apply { maxParents.set(value); noMerges.unset() }

    fun merges() = apply { merges.set(); minParents.unset() }
    fun noMerges() = apply { noMerges.set(); maxParents.unset() }

    fun range(value: RevisionRangeParam) = apply { revisionRange = value }

    override fun call(): Iterable<Commit> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(reflog, maxCount, skip, since, until, author, commmitter, firstParent)
            addAll(maxParents, minParents, merges, noMerges)
            add(revisionRange)
        }

        val cmd = "git --no-pager log"

        val subjects = Commit.CommitLineType.values().map { it.value to it.format }

        val runners = subjects.mapIndexed { index, pair ->
            val title = pair.first
            val format = pair.second
            GitRunner.create("$cmd $paramsBuilder --pretty=format:$title:$format")
        }

        val result = hashMapOf<Int, Commit>()

        GitRunner.execute(*runners.toTypedArray())
            .map { it.get() }
            .sortedBy { it.id }
            .forEach {
                it.readLines().mapIndexed { index, line ->
                    val commit = result.getOrPut(index) { Commit() }
                    val (type, text) = line.split(":".toRegex(), limit = 2)
                    commit.load(type, text)
                }
            }


        return result.values
    }

    data class RevisionRangeParam private constructor(
        private val first: String = Repository.HEAD,
        private val second: String? = null,
        private val type: RangeType = RangeType.Single
    ) : GitParam {

        override fun asQueryString(): String {
            return when (type) {
                RangeType.Single -> first
                RangeType.Range -> "$first..$second"
                RangeType.Symmetric -> "$first...$second"
            }
        }

        enum class RangeType {
            Single, Range, Symmetric
        }

        companion object {
            fun head() = RevisionRangeParam()
            fun from(value: String) = RevisionRangeParam(value, null, RangeType.Single)
            fun range(from: String, to: String) = RevisionRangeParam(from, to, RangeType.Range)
            fun simmetric(first: String, second: String) =
                RevisionRangeParam(first, second, RangeType.Symmetric)
        }
    }
}

class Commit {
    val values: MutableMap<CommitLineType, String?> = mutableMapOf()

    fun get(type: CommitLineType): String? = values[type]

    fun set(key: CommitLineType, value: String?) {
        values[key] = value
    }

    fun entries() = values.entries

    fun load(type: String, text: String) = set(CommitLineType.of(type), text)
    override fun toString(): String {
        return "Commit(${values.map { "${it.key.value}: ${it.value}" }.joinToString(", ")})"
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
