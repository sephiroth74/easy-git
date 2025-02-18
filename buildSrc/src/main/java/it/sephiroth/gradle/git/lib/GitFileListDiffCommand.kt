package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * Base command is: "git --no-pager --name-only"
 */
class GitFileListDiffCommand(repository: Repository) : GitCommand<List<String>>(repository) {
    // ----------------------------------------------------
    // region Git arguments

    private val nameOnly = GitNameValueParam<Unit>("--name-only").set()
    private val cached = GitNameValueParam<Unit>("--cached")

    // endregion Git arguments
    fun cached() = apply { cached.set() }

    fun nameOnly() = apply { nameOnly.set() }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(nameOnly, cached)
        }

        return GitRunner.execute("git --no-pager diff $paramsBuilder", repo.repoDir)
            .await()
            .assertNoErrors()
            .readLines(GitRunner.StdOutput.Output)
    }
}
