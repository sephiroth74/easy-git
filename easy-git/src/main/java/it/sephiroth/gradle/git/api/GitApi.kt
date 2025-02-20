package it.sephiroth.gradle.git.api

import org.gradle.api.logging.Logger

abstract class GitApi protected constructor(protected val git: Git) {
    protected val logger: Logger by lazy { Git.logger }
}
