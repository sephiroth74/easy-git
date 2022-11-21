package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

class GitDeleteTagCommand(repo: Repository, private val tagName: String) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    // endregion git arguments

    override fun call(): String {
        return GitRunner.execute("git tag -d $tagName", repo.repoDir)
            .await()
            .assertNoErrors()
            .readText(GitRunner.StdOutput.Output) ?: "Success"
    }

}
