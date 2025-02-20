package it.sephiroth.gradle.git.api

import it.sephiroth.gradle.git.lib.GitFetchCommand
import it.sephiroth.gradle.git.lib.GitDeleteTagCommand
import it.sephiroth.gradle.git.lib.GitTagListCommand
import it.sephiroth.gradle.git.lib.GitTagAddCommand

class GitTag internal constructor(git: Git) : GitApi(git) {

    fun fetch() = GitFetchCommand(git.repository).tags()

    fun delete(name: String) = GitDeleteTagCommand(git.repository, name)

    fun list() = GitTagListCommand(git.repository)

    fun add(tagName: String) = GitTagAddCommand(git.repository, tagName)


//
//
//    fun fetch() = GitExecutor.fetchTags(git.jgit, dryRun = false, force = true)
//
//    fun list() = GitExecutor.getTagList(git.jgit)
//
//    fun delete(vararg tags: String) = GitExecutor.deleteTag(git.jgit, *tags)
//
//    fun fetch(prune: Boolean) {
//        val args = mutableListOf<String>().apply {
//            if (prune) add("--prune")
//        }.joinToString(" ")
//
//        deleteAll()
//        "git fetch --tags $args".exec().run { assertExitCode() }
//    }
//
//    fun deleteAll() {
//        "git --no-pager tag -l".exec().assertExitCode().readLines().forEach { line -> "git tag -d $line".exec().assertExitCode() }
//    }


//
//    fun list(): List<Tag> {
//        val lines = ProcessBuilder(
//            "git",
//            "--no-pager",
//            "tag",
//            "-l",
//            """--format={ "name": "%(refname:lstrip=2)", "author": { "name":"%(*authorname)", "email":"%(*authoremail)", "date":"%(*authordate:iso-strict)" }, "ref":"%(refname)", "subject":"%(*subject:sanitize)" }"""
//        )
//            .directory(git.baseDir)
//            .redirectOutput(ProcessBuilder.Redirect.PIPE)
//            .start().run {
//                assertExitCode()
//                readLines()
//            }
//
//        return lines.map { line ->
//            println("line -> $line")
//            git.gson.fromJson(line)
//        }
//    }
}
