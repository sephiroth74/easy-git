package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-cat-file">git cat-file</a>
 */
class GitCatFileCommand(repo: Repository) : GitCommand<String>(repo) {
    // ----------------------------------------------------
    // region git arguments

    private var mode = Mode.Exists
    private var pathSpec: String? = null

    // endregion git arguments

    fun mode(value: Mode) = apply { this.mode = value }
    fun pathSpec(value: String) = apply { this.pathSpec = value }

    override fun call(): String {
        val paramsBuilder = ParamsBuilder().apply {
            add(mode)
            add(pathSpec)
        }

        val cmd = "git cat-file $paramsBuilder"
        return GitRunner.execute(cmd)
            .await()
            .assertNoErrors()
            .readText(GitRunner.StdOutput.Output) ?: ""
    }

    enum class Mode(private val value: String) : GitParam {
        Type("-t"), Exists("-e"), Size("-s"), PrettyPrint("-p");

        override fun asQueryString(): String? = this.value
    }
}
