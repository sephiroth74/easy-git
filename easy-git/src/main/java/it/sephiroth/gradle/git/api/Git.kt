package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.Repository
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logging
import java.io.File

class Git private constructor(
    val repository: Repository
) {
    val branch = GitBranch(this)
    val commit = GitCommit(this)
    val tag = GitTag(this)
    val diff = GitDiff(this)
    val log = GitLog(this)

    companion object {
        internal val logger = Logging.getLogger(Git::class.java)

        fun open(workDir: Directory): Git = open(workDir.asFile)

        fun open(workDir: File): Git = Git(Repository(workDir))
    }
}



