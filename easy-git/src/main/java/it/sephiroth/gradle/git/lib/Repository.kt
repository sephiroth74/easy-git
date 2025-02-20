package it.sephiroth.gradle.git.lib

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class Repository(val repoDir: File) {
    internal val builder: GsonBuilder = GsonBuilder()
    internal val gson: Gson = builder.create()

    /**
     * Returns the current commit hash
     * @param short use the short name
     */
    fun commitHash(short: Boolean): String = resolve(Repository.HEAD).apply { if (short) short() }.call().first()

    fun resolve(commitId: String = HEAD) = GitRevParseCommand(this).commitId(commitId)

    fun revList() = GitRevListCommand(this)

    fun lsRemote() = GitLsRemoteCommand(this)

    fun checkout() = GitCheckoutCommand(this)

    fun describe(refSpec: String? = null) = GitDescribeCommand(this, refSpec)

    fun add(vararg files: String) = GitAddCommand(this, *files)

    fun add(vararg files: File) = GitAddCommand(this, *files.map { it.name }.toTypedArray())

    fun commit() = GitCommitCommand(this)

    fun push() = GitPushCommand(this)

    fun status() = GitStatusCommand(this)

    companion object {
        const val HEAD: String = "HEAD"

        const val REF_HEADS = "refs/heads"

        const val REF_REMOTES = "refs/remotes"
    }
}
