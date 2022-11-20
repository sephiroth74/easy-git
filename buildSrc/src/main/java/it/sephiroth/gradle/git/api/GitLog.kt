package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.GitLogCommand

class GitLog internal constructor(git: Git) : GitApi(git) {

    fun head() = GitLogCommand(git.repository).range(GitLogCommand.RevisionRangeParam.head())

    fun range(from: String, to: String) = GitLogCommand(git.repository).range(GitLogCommand.RevisionRangeParam.range(from, to))

    fun symmetric(from: String, to: String) = GitLogCommand(git.repository).range(GitLogCommand.RevisionRangeParam.simmetric(from, to))

}
