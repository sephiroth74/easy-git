package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.exception.GitExecutionException
import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.utils.EscapedString

/**
 * @see <a href="">git ls-remote</a>
 */
class GitLsRemoteCommand(repo: Repository) : GitCommand<List<String>>(repo) {
    // ----------------------------------------------------
    // region git arguments

    private val heads = GitNameParam("--heads")
    private val tags = GitNameParam("--tags")
    private val refs = GitNameParam("--refs")
    private val symref = GitNameParam("--symref")
    private val sort = GitNameValueParam<EscapedString>("--sort")
    private var repository: String? = null

    // endregion git arguments

    /**
     * For instance: --sort="-v:committerdate"
     */
    fun sort(value: String) = apply { sort.set(EscapedString(value)) }
    fun heads() = apply { heads.set() }
    fun tags() = apply { tags.set() }
    fun refs() = apply { refs.set() }
    fun symref() = apply { symref.set() }
    fun repository(name: String) = apply { repository = name }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            add(heads)
            add(tags)
            add(refs)
            add(repository)
        }
        val cmd = "git --no-pager ls-remote --quiet $paramsBuilder"

        return GitRunner.execute(cmd, repo.repoDir)
            .await()
            .assertNoErrors()
            .readLines(GitRunner.StdOutput.Output).mapNotNull {
                val line = it.split("[\\t\\s]+".toRegex(), 2)
                val commitId = line.first()
                val objectName = line.last()

                try {
                    GitCatFileCommand(repo)
                        .mode(GitCatFileCommand.Mode.PrettyPrint)
                        .pathSpec(commitId)
                        .call()
                } catch (e: GitExecutionException) {
//                    println("object $objectName does not exists")
                    null
                    // ignore
                }

            }
    }
}
