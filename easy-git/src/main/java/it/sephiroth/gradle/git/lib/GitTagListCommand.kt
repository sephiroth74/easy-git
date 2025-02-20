package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.utils.EscapedString

/**
 * @see <a href="https://git-scm.com/docs/git-tag">git-tag</a>
 */
class GitTagListCommand(repo: Repository) : GitCommand<List<String>>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val num = GitNumParam<Int>("-n")
    private val contains = GitNameValueParam<String>("--contains")
    private val noContains = GitNameValueParam<String>("--no-contains")
    private val pointsAt = GitNameValueParam<String>("--points-at")
    private val column = GitNameValueParam<String>("--column")
    private val noColumn = GitNameParam("--no-column")
    private val sort = GitNameValueParam<String>("--sort")
    private val format = GitNameValueParam<String>("--format")
    private val merged = GitNameValueParam<String>("--merged")
    private val noMerged = GitNameValueParam<String>("--no-merged")

    private var pattern: String? = null

    // endregion git arguments

    fun pattern(value: String?) = apply { pattern = value }
    fun noMerged(value: String? = null) = apply { noMerged.set(value) }
    fun merged(value: String? = null) = apply { merged.set(value) }
    fun format(value: String) = apply { format.set(value) }
    fun sort(value: String) = apply { sort.set(value) }
    fun column(value: String) = apply { column.set(value) }
    fun pointsAt(value: String? = null) = apply { pointsAt.set(value) }
    fun noContains(value: String? = null) = apply { noContains.set(value) }
    fun contains(value: String? = null) = apply { contains.set(value) }
    fun num(value: Int) = apply { num.set(value) }
    fun noColumn() = apply { noColumn.set() }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            add(num)
            addAll(contains, noContains, pointsAt, column, noContains, sort, format, merged, noMerged)
            add(pattern)
        }

        val commands = mutableListOf("git", "--no-pager", "tag", "-l").apply {
            addAll(paramsBuilder.toList())
        }

        return GitRunner.execute(commands, repo.repoDir)
            .await()
            .assertNoErrors()
            .readLines(GitRunner.StdOutput.Output)
    }
}
