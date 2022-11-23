package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-remote">git remote</a>
 */
class GitRemoteGetCommand(repo: Repository, private val name: String) : GitCommand<String?>(repo) {

    // ----------------------------------------------------
    // region git arguments

    // endregion git arguments

    override fun call(): String? {
        val commands = mutableListOf("git", "config", "remote.$name.url")
        return GitRunner.execute(commands, repo.repoDir).await().assertNoErrors().readText()
    }
}
