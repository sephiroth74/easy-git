package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.exception.GitExecutionException
import java.io.File

/**
 * @see <a href="https://git-scm.com/docs/git-add">git add</a>
 */
class GitAddCommand(repo: Repository, vararg files: String) : GitCommand<List<String>>(repo) {
    private val files: MutableList<String> = files.toMutableList()

    // ----------------------------------------------------
    // region git arguments

    private val dryRun = GitNameParam("--dry-run")
    private val verbose = GitNameParam("--verbose")
    private val force = GitNameParam("--force")
    private val refresh = GitNameParam("--refresh")
    private val ignoreErrors = GitNameParam("--ignore-errors")
    private val ignoreMissing = GitNameParam("--ignore-missing")

    // endregion git arguments

    fun dryRun() = apply { dryRun.set() }
    fun verbose() = apply { verbose.set() }
    fun force() = apply { force.set() }
    fun refresh() = apply { refresh.set() }
    fun ignoreErrors() = apply { ignoreErrors.set() }
    fun ignoreMissing() = apply { ignoreMissing.set() }

//    private fun getRelativePaths(): List<String> {
//        return files.map {
//            val path = it.toPath()
//            if (path.isAbsolute) {
//                repo.repoDir.toPath().normalize().relativize(path)
//            }
//            path.toString()
//        }
//    }

    override fun call(): List<String> {
        if (files.isEmpty()) throw GitExecutionException("No files provided")
        val paramsBuilder = ParamsBuilder().apply {
            addAll(dryRun, force, refresh, ignoreErrors, ignoreMissing, verbose)
            if (files.isNotEmpty()) {
                add("--")
                addAll(files)
            }
        }

        val cmd = "git add $paramsBuilder"
        return GitRunner.execute(cmd, repo.repoDir).await().assertNoErrors().readLines()
    }
}
