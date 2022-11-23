package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.GitCommand.RevisionRangeParam
import it.sephiroth.gradle.git.lib.GitLogCommand
import it.sephiroth.gradle.git.lib.*

class GitLog internal constructor(git: Git) : GitApi(git) {

    fun count() = GitLogCountCommand(git.repository)

    fun head() = GitLogCommand(git.repository).range(RevisionRangeParam.head())

    fun range(from: String, to: String) =
        GitLogCommand(git.repository).range(RevisionRangeParam.range(from, to))

    fun push(direction: RevisionRangeParam.Direction) =
        GitLogCommand(git.repository).range(RevisionRangeParam.push(direction))

    fun symmetric(from: String, to: String) =
        GitLogCommand(git.repository).range(RevisionRangeParam.simmetric(from, to))

}
