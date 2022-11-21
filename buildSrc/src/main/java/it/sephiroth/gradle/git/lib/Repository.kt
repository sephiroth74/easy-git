package it.sephiroth.gradle.git.lib

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class Repository(val repoDir: File) {
    internal val builder: GsonBuilder = GsonBuilder()
    internal val gson: Gson = builder.create()

    fun resolve(commitId: String = HEAD) = GitRevParseCommand(this).commitId(commitId)

    fun revList() = GitRevListCommand(this)

    fun lsRemote() = GitLsRemoteCommand(this)

    fun checkout() = GitCheckoutCommand(this)

    fun describe(refSpec: String? = null) = GitDescribeCommand(this, refSpec)

    fun add(vararg files: File) = GitAddCommand(this, *files)

    fun commit() = GitCommitCommand(this)

    fun push() = GitPushCommand(this)

    companion object {
        const val HEAD: String = "HEAD"

        const val REF_HEADS = "refs/heads"

        const val REF_REMOTES = "refs/remotes"
    }
}
