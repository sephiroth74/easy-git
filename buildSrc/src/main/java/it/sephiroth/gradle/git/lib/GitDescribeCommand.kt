package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.utils.EscapedString

/**
 * @see <a href="https://git-scm.com/docs/git-describe">git describe</a>
 */
class GitDescribeCommand(repo: Repository, refSpec: String? = null) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val all = GitNameParam("--all")
    private val tags = GitNameParam("--tags")
    private val contains = GitNameParam("--contains")
    private val exactMatch = GitNameParam("--exact-match")
    private val long = GitNameParam("--long")
    private val match = GitNameValueParam<EscapedString>("--match")
    private val exclude = GitNameValueParam<EscapedString>("--exclude")
    private var pathRefSpec: String? = refSpec

    private val abbrev = GitNameValueParam<Int>("--abbrev")
    private val candidates = GitNameValueParam<Int>("--candidates")

    // endregion git arguments

    fun all() = apply { all.set() }
    fun tags() = apply { tags.set() }
    fun contains() = apply { contains.set() }
    fun exactMatch() = apply { exactMatch.set() }
    fun long() = apply { long.set(); abbrev.unset() }
    fun abbrev(value: Int) = apply { abbrev.set(value); long.unset() }

    fun match(value: String?) = apply { match.set(EscapedString(value)) }
    fun exclude(value: String?) = apply { exclude.set(EscapedString(value)) }
    fun candidates(value: Int) = apply { candidates.set(value) }
    fun refSpec(value: String) = apply { pathRefSpec = value }

    override fun call(): String {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(all, tags, contains, exactMatch, long, match, exclude)
            addAll(abbrev, candidates)
            add(pathRefSpec)
        }

        val commands = mutableListOf("git", "describe").apply {
            addAll(paramsBuilder.toList())
        }

        return GitRunner.execute(commands)
            .await()
            .assertNoErrors()
            .readText(GitRunner.StdOutput.Output) ?: ""
    }
}
