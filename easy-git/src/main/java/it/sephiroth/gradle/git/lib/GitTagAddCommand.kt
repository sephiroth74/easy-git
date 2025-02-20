package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.utils.EscapedString

/**
 * @see <a href="https://git-scm.com/docs/git-tag">git tag</a>
 */
class GitTagAddCommand(repo: Repository, private val tagName: String) : GitCommand<String>(repo) {
    // ----------------------------------------------------
    // region git arguments

    private val force = GitNameParam("--force")
    private val message = GitNameValueParam<EscapedString>("--message")
    private val file = GitNameValueParam<String>("--file")

    // endregion git arguments

    fun force() = apply { force.set() }

    fun message(value: String?) = apply {
        value?.let {
            message.set(EscapedString(it))
            file.unset()
        } ?: run {
            message.unset()
        }
    }

    fun file(value: String?) = apply {
        value?.let {
            file.set(it)
            message.unset()
        } ?: run {
            file.unset()
        }
    }

    override fun call(): String {
        val paramsBuilder = ParamsBuilder().apply {
            add(force)
            add(message)
            add(file)
            add(tagName)
        }

        val commands = mutableListOf("git", "tag").apply {
            addAll(paramsBuilder.toList())
        }

        return GitRunner.execute(commands, repo.repoDir).await().assertNoErrors().readText() ?: ""
    }
}
