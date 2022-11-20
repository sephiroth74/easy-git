package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import java.time.OffsetDateTime

/**
 * @see <a href="https://git-scm.com/docs/git-rev-list">git rev-list</a>
 */
class GitRevListCommand(repo: Repository) : GitCommand<List<String>>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val maxCount = GitNameValueParam<Int>("--max-count")
    private val skip = GitNameValueParam<Int>("--skip")
    private val since = GitNameValueParam<OffsetDateTime>("--since")
    private val until = GitNameValueParam<OffsetDateTime>("--until")
    private val maxAge = GitNameValueParam<Long>("--max-age")
    private val minAge = GitNameValueParam<Long>("--min-age")
    private val author = GitNameValueParam<String>("--author")
    private val committer = GitNameValueParam<String>("--committer")
    private val merges = GitNameParam("--merges")
    private val noMerges = GitNameParam("--no-merges")
    private val minParents = GitNameValueParam<Int>("--min-parents")
    private val maxParents = GitNameValueParam<Int>("--max-parents")

    private val tags = GitNameValueParam<String>("--tags")
    private val remotes = GitNameValueParam<String>("--remotes")
    private val branches = GitNameValueParam<String>("--branches")
    private val glob = GitNameValueParam<String>("--glob")

    private val all = GitNameParam("--all")

    // endregion git arguments

    fun maxCount(value: Int) = apply { maxCount.set(value) }
    fun skip(value: Int) = apply { skip.set(value) }
    fun since(value: OffsetDateTime) = apply { since.set(value) }
    fun until(value: OffsetDateTime) = apply { until.set(value) }
    fun maxAge(value: Long) = apply { maxAge.set(value) }
    fun minAge(value: Long) = apply { minAge.set(value) }
    fun author(value: String) = apply { author.set(value) }
    fun committer(value: String) = apply { author.set(value) }
    fun merges() = apply { merges.set(); minParents.unset() }
    fun noMerges() = apply { noMerges.set(); maxParents.unset() }
    fun maxParents(value: Int) = apply { maxParents.set(); merges.unset() }
    fun minParents(value: Int) = apply { minParents.set(); noMerges.unset() }

    fun tags(value: String? = null) = apply { tags.set(value) }
    fun remotes(value: String? = null) = apply { remotes.set(value) }
    fun branches(value: String? = null) = apply { branches.set(value) }
    fun glob(value: String? = null) = apply { glob.set(value) }

    fun all() = apply { all.set() }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            add(maxCount)
            add(skip)
            add(since)
            add(until)
            add(maxAge)
            add(minAge)
            add(author)
            add(committer)
            add(merges)
            add(noMerges)
            add(minParents)
            add(maxParents)
            addAll(tags, remotes, branches, glob, all)
        }

        val cmd = "git --no-pager rev-list $paramsBuilder"
        return GitRunner.execute(cmd)
            .await()
            .assertNoErrors()
            .readLines(GitRunner.StdOutput.Output)
    }

}
