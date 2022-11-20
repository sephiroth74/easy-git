package it.sephiroth.gradle.git.lib

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class Repository(val repoDir: File) {
    internal val builder: GsonBuilder = GsonBuilder()
    internal val gson: Gson = builder.create()

    fun resolve(commitId: String = HEAD) = GitRevParseCommand(this).commitId(commitId)

    fun lsRemote() = GitLsRemoteCommand(this)

    fun checkout() = GitCheckoutCommand(this)

    companion object {
        const val HEAD: String = "HEAD"

        const val REF_HEADS = "refs/heads"

        const val REF_REMOTES = "refs/remotes"
    }
}
