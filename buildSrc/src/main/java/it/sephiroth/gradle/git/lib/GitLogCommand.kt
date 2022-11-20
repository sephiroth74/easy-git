package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import java.time.OffsetDateTime

/**
 * @see <a href="">git log</a>
 */
class GitLogCommand(repo: Repository) : GitCommand<List<String>>(repo) {

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

    private val minParents = GitNameValueParam<Int>("--min-parents")
    private val maxParents = GitNameValueParam<Int>("--max-parents")

    private var revisionRange = RevisionRangeParam.head()

    // endregion git arguments

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

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(reflog, maxCount, skip, since, until, author, commmitter)
            addAll(maxParents, minParents, merges, noMerges)
            add(revisionRange)
        }

        val subjects = listOf<Pair<String, String>>(
            "commit" to "%H",
            "tree" to "%T",
            "parent" to "%P",
            "subject" to "%s",
            "author" to "%an <%ae> %ai",
            "name" to "%(describe:abbrev=4,tags=true)",
            "tags" to "%D"
        )

        val cmd = "git --no-pager log"

        // git --no-pager log -n1 --pretty=
        // format:'{%n  "commit":"%H",%n  "tree":"%T",%n  "parent":"%P",%n "subject":"%s",%n  "body":"%b",%n  "author":{%n    "name":"%aN",%n    "email":"%aE",%n    "date":"%ai"%n  }%n
        // "name":"%(describe:abbrev=4,tags=true)"%n  "tags":"%D" %n},'

        subjects.forEach { pair ->
            val title = pair.first
            val format = pair.second
            val text = GitRunner.execute("$cmd --pretty=format:$format $paramsBuilder").await().assertNoErrors()
                .readText(GitRunner.StdOutput.Output)
            println("text => $text")

        }

        return emptyList()
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
            fun simmetric(first: String, second: String) = RevisionRangeParam(first, second, RangeType.Symmetric)
        }
    }
}

