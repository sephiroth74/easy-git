package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.GitFileListDiffCommand

/**
 * <a href="https://git-scm.com/docs/git-diff">git diff</a>
 */
class GitDiff internal constructor(git: Git) : GitApi(git) {
    fun fileList() = GitFileListDiffCommand(git.repository)
}
