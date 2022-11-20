package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.GitRevParseCommand
import it.sephiroth.gradle.git.lib.GitBranchListCommand
import it.sephiroth.gradle.git.lib.Repository

class GitBranch internal constructor(git: Git) : GitApi(git) {

    /**
     * Returns the current branch name
     */
    fun name(): String? {
        return git.repository
            .resolve(Repository.HEAD)
            .abbrevRef(GitRevParseCommand.AbbrevRefMode.Strict)
            .call()
            .firstOrNull()
    }

    fun list() = GitBranchListCommand(git.repository)

//    fun list(listMode: ListMode? = null) = GitExecutor.getBranchList(git.jgit, listMode) ?: emptyList()
}
