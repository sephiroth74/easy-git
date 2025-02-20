package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-branch">git branch</a>
 */
class GitDeleteLocalBranchCommand(repo: Repository, private val branchName: String) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val delete = GitNameParam("--delete").set()
    private val force = GitNameParam("--force")
    private val removeTracking = GitNameParam("-r")

    // endregion git arguments

    fun force() = apply { force.set() }
    fun removeTracking() = apply { removeTracking.set() }

    override fun call(): String {
        val paramsBuilder = ParamsBuilder().apply {
            add(delete)
            add(force)
            add(removeTracking)
        }

        val commands = mutableListOf("git", "branch").apply {
            addAll(paramsBuilder.toList())
            add(branchName)
        }
        return GitRunner.execute(commands, repo.repoDir).await().assertNoErrors().readText() ?: ""
    }

}
