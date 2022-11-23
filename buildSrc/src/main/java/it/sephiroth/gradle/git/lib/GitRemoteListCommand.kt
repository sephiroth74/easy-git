package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-remote">git remote</a>
 */
class GitRemoteListCommand(repo: Repository) : GitCommand<List<String>>(repo) {

    // ----------------------------------------------------
    // region git arguments

    // endregion git arguments

    override fun call(): List<String> {
        val commands = mutableListOf("git", "remote")
        return GitRunner.execute(commands, repo.repoDir).await().assertNoErrors().readLines()
    }
}
