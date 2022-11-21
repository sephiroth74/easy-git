package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.utils.EscapedString

/**
 * @see <a href="https://git-scm.com/docs/git-commit">git commit</a>
 */
class GitCommitCommand(repo: Repository) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val message = GitNameValueParam<EscapedString>("--message")
    private val reuseMessage = GitNameValueParam<EscapedString>("--reuse-message")
    private val all = GitNameParam("--all")
    private val short = GitNameParam("--short")
    private val porcelain = GitNameParam("--porcelain")
    private val file = GitNameValueParam<String>("--file")
    private val author = GitNameValueParam<EscapedString>("--author")
    private val allowEmptyMessage = GitNameParam("--allow-empty-message")
    private val amend = GitNameParam("--amend")
    private val quiet = GitNameParam("--quiet")
    private val dryRun = GitNameParam("--dry-run")

    // endregion git arguments

    fun porcelain() = apply { porcelain.set() }

    fun quiet() = apply { quiet.set() }

    fun dryRun() = apply { dryRun.set() }

    fun amend() = apply { amend.set() }

    fun allowEmptyMessage() = apply { allowEmptyMessage.set() }

    fun short() = apply { short.set() }

    fun all() = apply { all.set() }

    fun author(value: String) = apply { author.set(EscapedString(value)) }

    fun file(path: String) = apply { file.set(path); message.unset(); reuseMessage.unset() }

    fun message(msg: String) = apply { message.set(EscapedString(msg)); reuseMessage.unset(); file.unset() }

    fun reuseMessage(msg: String) = apply { reuseMessage.set(EscapedString(msg)); message.unset(); file.unset() }

    override fun call(): String {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(message, reuseMessage, file)
            addAll(short, author, allowEmptyMessage, amend)
            addAll(quiet, dryRun, porcelain)
        }

        val cmd = mutableListOf("git", "--no-pager", "commit").apply { addAll(paramsBuilder.toList()) }
        return GitRunner.execute(cmd).await().assertNoErrors().readText() ?: ""
    }
}
