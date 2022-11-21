package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import java.io.File

/**
 * @see <a href="https://git-scm.com/docs/git-add">git add</a>
 */
class GitAddCommand(repo: Repository, vararg files: File) : GitCommand<List<String>>(repo) {
    private val files: MutableList<File> = files.toMutableList()

    // ----------------------------------------------------
    // region git arguments

    private val dryRun = GitNameParam("--dry-run")
    private val force = GitNameParam("--force")
    private val refresh = GitNameParam("--refresh")
    private val ignoreErrors = GitNameParam("--ignore-errors")
    private val ignoreMissing = GitNameParam("--ignore-missing")

    // endregion git arguments

    fun dryRun() = apply { dryRun.set() }
    fun force() = apply { force.set() }
    fun refresh() = apply { refresh.set() }
    fun ignoreErrors() = apply { ignoreErrors.set() }
    fun ignoreMissing() = apply { ignoreMissing.set() }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(dryRun, force, refresh, ignoreErrors, ignoreMissing)
            addAll(files.map {
                val path = it.toPath()
                if (path.isAbsolute) {
                    path.relativize(repo.repoDir.toPath()).toString()
                } else {
                    path.toString()
                }
            })
        }

        val cmd = "git add $paramsBuilder"
        return GitRunner.execute(cmd).await().assertNoErrors().readLines()
    }
}
