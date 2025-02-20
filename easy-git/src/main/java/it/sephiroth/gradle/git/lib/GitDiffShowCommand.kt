package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner
import java.io.File

/**
 * @see <a href="https://git-scm.com/docs/git-diff">git diff</a>
 */
class GitDiffShowCommand(repo: Repository) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val diffFilter = GitNameValueParam<String>("--diff-filter")
    private var revisionRange = RevisionRangeParam.head()
    private var paths: MutableList<File> = mutableListOf()

    // endregion git arguments

    fun paths(vararg files: File) = apply {
        paths.clear()
        paths.addAll(files)
    }

    fun paths(vararg files: String) = apply {
        paths.clear()
        paths.addAll(files.map { File(it) })
    }

    fun commits(range: RevisionRangeParam) = apply { revisionRange = range }

    fun diffFilter(value: String) = apply { diffFilter.set(value) }

    override fun call(): String {
        val paramsBuilder = ParamsBuilder().apply {
            add(diffFilter)
            add(revisionRange)
        }

        val commands = mutableListOf("git", "--no-pager", "diff").apply {
            addAll(paramsBuilder.toList())
            if (paths.isNotEmpty()) {
                add("--")
                addAll(paths.map { it.toString() })
            }
        }

        return GitRunner.execute(commands, repo.repoDir).await().assertNoErrors().readText() ?: ""
    }
}
