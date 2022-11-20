package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.GitCommand.RevisionRangeParam
import it.sephiroth.gradle.git.lib.GitLogCommand

class GitLog internal constructor(git: Git) : GitApi(git) {

    fun head() = GitLogCommand(git.repository).range(RevisionRangeParam.head())

    fun range(from: String, to: String) =
        GitLogCommand(git.repository).range(RevisionRangeParam.range(from, to))

    fun symmetric(from: String, to: String) =
        GitLogCommand(git.repository).range(RevisionRangeParam.simmetric(from, to))

}
