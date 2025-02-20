package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.vo.LogCommit
import java.time.OffsetDateTime
import java.util.regex.Pattern

/**
 * @see <a href="https://git-scm.com/docs/git-log">git log</a>
 */
@Suppress("unused")
class GitLogCountCommand internal constructor(repo: Repository) : GitCommand<Int>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val maxCount = GitNameValueParam<Int>("--max-count")
    private val skip = GitNameValueParam<Int>("--skip")
    private val since = GitNameValueParam<OffsetDateTime>("--since")
    private val until = GitNameValueParam<OffsetDateTime>("--until")
    private val author = GitNameValueParam<String>("--author")
    private val commmitter = GitNameValueParam<String>("--committer")

    // required for commits parsing
    private val merges = GitNameParam("--merges")
    private val noMerges = GitNameParam("--no-merges")
    private val reflog = GitNameParam("--reflog")
    private val firstParent = GitNameParam("--first-parent")

    private val minParents = GitNameValueParam<Int>("--min-parents")
    private val maxParents = GitNameValueParam<Int>("--max-parents")

    private var revisionRange: RevisionRangeParam? = null

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

    override fun call(): Int {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(reflog, maxCount, skip, since, until, author, commmitter, firstParent)
            addAll(maxParents, minParents, merges, noMerges)
            add(revisionRange)
        }

        val commands = mutableListOf("git", "--no-pager", "log", "--pretty=oneline").apply {
            addAll(paramsBuilder.toList())
        }

        return GitRunner.execute(commands, repo.repoDir).await().assertNoErrors().readLines().size
    }

    private fun parseLogsText(
        tag: String,
        logText: String,
        result: HashMap<Int, LogCommit>
    ) {
        var index = 0
        var commitIndex = 0
        while (index < logText.length) {
            val match = logSizeReg.find(logText, index)
            val range = match!!.range
            val logSize = match.groupValues[1].toInt()
            val logMessage = logText.substring(range.last + 1, range.last + 1 + logSize)
            index = range.last + 1 + logSize
            val commit = result.getOrPut(commitIndex++) { LogCommit() }
            commit.load(tag, logMessage)
        }
    }

    companion object {
        private val logSizeReg =
            Pattern.compile("^log size ([0-9]+)\n", Pattern.MULTILINE).toRegex()
    }
}

