package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.GitRemoteAddCommand
import it.sephiroth.gradle.git.lib.GitRemoteGetCommand
import it.sephiroth.gradle.git.lib.GitRemoteListCommand
import it.sephiroth.gradle.git.lib.GitRemoteRemoveCommand

class GitRemote internal constructor(git: Git) : GitApi(git) {

    fun remove(name: String) = GitRemoteRemoveCommand(git.repository, name)

    fun add(name: String, url: String) = GitRemoteAddCommand(git.repository, name, url)

    fun get(name: String) = GitRemoteGetCommand(git.repository, name)

    fun list() = GitRemoteListCommand(git.repository)

}
