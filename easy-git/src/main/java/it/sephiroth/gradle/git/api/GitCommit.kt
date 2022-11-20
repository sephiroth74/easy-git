package it.sephiroth.gradle.git.api


class GitCommit internal constructor(git: Git) : GitApi(git) {

    fun hash(short: Boolean = false): String? {
        TODO("implement this")
    }
}
