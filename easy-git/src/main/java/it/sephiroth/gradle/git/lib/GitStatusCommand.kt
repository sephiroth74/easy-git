package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.vo.StatusResult

/**
 * @see <a href="https://git-scm.com/docs/git-status">git status</a>
 */
@Suppress("unused")
class GitStatusCommand(repo: Repository) : GitCommand<StatusResult>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val short = GitNameParam("--short").set()
    private val branch = GitNameParam("--branch")
    private var pathSpec: String? = null

    // endregion git arguments

    fun branch() = apply { branch.set() }
    fun pathSpec(value: String?) = apply { pathSpec = value }

    override fun call(): StatusResult {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(short, branch)
            add(pathSpec)
        }

        val commands = mutableListOf("git", "status").apply { addAll(paramsBuilder.toList()) }
        return StatusResult.parse(GitRunner.execute(commands, repo.repoDir).await().assertNoErrors().readLines())
    }


}
