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

    override fun call(): Iterable<LogCommit> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(reflog, maxCount, skip, since, until, author, commmitter, firstParent)
            addAll(maxParents, minParents, merges, noMerges)
            add(revisionRange)
        }

        val cmd = "git --no-pager log"

        val subjects = LogCommit.CommitLineType.values().map { it.value to it.format }

        val runners: List<GitRunner> = subjects.mapIndexed { _, pair ->
            val title = pair.first
            val format = pair.second
            GitRunner.create("$cmd $paramsBuilder --pretty=format:$title:$format", title)
        }

        val result = hashMapOf<Int, LogCommit>()

        GitRunner.execute(*runners.toTypedArray())
            .map { it.get() }
            .forEach { runner ->
                val tag: String = runner.tag as String
                val regexp =
                    Pattern.compile("^($tag):(.*)", Pattern.DOTALL).toRegex()

                runner.readLines().mapIndexed { index, line ->
                    val commit = result.getOrPut(index) { LogCommit() }
//                    val (type, text) = line.split(":".toRegex(), limit = 2)

                    regexp.matchEntire(line)?.let { match ->
                        commit.load(tag, match.groupValues[2])
                    }
                }
            }

        return result.values
    }
}

