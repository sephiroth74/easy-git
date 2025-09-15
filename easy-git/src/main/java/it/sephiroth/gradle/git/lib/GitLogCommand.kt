package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.vo.LogCommit
import java.time.OffsetDateTime
import java.util.regex.Pattern

/**
 * @see <a href="https://git-scm.com/docs/git-log">git log</a>
 */
@Suppress("unused")
class GitLogCommand(repo: Repository) : GitCommand<Iterable<LogCommit>>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val maxCount = GitNameValueParam<Int>("--max-count")
    private val skip = GitNameValueParam<Int>("--skip")
    private val since = GitNameValueParam<OffsetDateTime>("--since")
    private val until = GitNameValueParam<OffsetDateTime>("--until")
    private val author = GitNameValueParam<String>("--author")
    private val commmitter = GitNameValueParam<String>("--committer")

    // required for commits parsing
    private val logSize = GitNameParam("--log-size").set()

    private val merges = GitNameParam("--merges")
    private val noMerges = GitNameParam("--no-merges")
    private val reflog = GitNameParam("--reflog")
    private val firstParent = GitNameParam("--first-parent")

    private val minParents = GitNameValueParam<Int>("--min-parents")
    private val maxParents = GitNameValueParam<Int>("--max-parents")

    private val maxLength = GitNameValueParam<Int>("--max-length")

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

    fun maxLength(value: Int) = apply { maxLength.set(value) }

    fun range(value: RevisionRangeParam) = apply { revisionRange = value }

    override fun call(): Iterable<LogCommit> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(reflog, maxCount, skip, since, until, author, commmitter, firstParent)
            addAll(maxParents, minParents, merges, noMerges, logSize)
            add(revisionRange)
        }

        val cmd = "git --no-pager log"
        val subjects = LogCommit.CommitLineType.values().map { it.value to it.format }

        val runners: List<GitRunner> = subjects.mapIndexed { _, pair ->
            val title = pair.first
            val format = pair.second
            GitRunner.create("$cmd $paramsBuilder --pretty=format:$format", title, repo.repoDir)
        }

        val result = hashMapOf<Int, LogCommit>()


        GitRunner.execute(*runners.toTypedArray())
            .map { it.get() }
            .forEach { runner ->
                val tag: String = runner.tag as String
                runner.readText(GitRunner.StdOutput.Output)?.let { logText ->
                    parseLogsText(tag, logText, result)
                } ?: run {
                    // We have a problem
                }
            }

        return result.values
    }

    private fun parseLogsText(
        tag: String,
        logText: String,
        result: HashMap<Int, LogCommit>
    ) {
        var index = 0
        var commitIndex = 0

        var text = logText
        var byteArray = text.toByteArray(charset = Charsets.UTF_8)

        while (text.isNotEmpty() || byteArray.isNotEmpty()) {
            val match = logSizeReg.find(text)
            index++
//            check(index <= INDEX_LIMIT) { "index > $INDEX_LIMIT" }


            if (null == match) {
                break
            }

            val range = match.range
            val logSize = match.groupValues[1].toInt()

            var logMessage = byteArray
                .copyOfRange(range.last + 1, range.last + 1 + logSize)
                .toString(Charsets.UTF_8).trim()

//            byteArray = byteArray.copyOfRange(range.last + 1 + logSize, byteArray.size)
//            text = byteArray.toString(Charsets.UTF_8)
            if (maxLength.isPreset) {
                logMessage = logMessage.take(maxLength.value!!)
            }
            val commit = result.getOrPut(commitIndex++) { LogCommit() }
            commit.load(tag, logMessage)
        }
    }

    companion object {
        private val logSizeReg =
            Pattern.compile("^log size ([0-9]+)\n", Pattern.MULTILINE).toRegex()

        const val INDEX_LIMIT = 200
    }
}

