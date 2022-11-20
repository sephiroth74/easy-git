package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * Executes the rev-parse command
 * @see <a href="https://git-scm.com/docs/git-rev-parse">git rev-parse</a>
 */
class GitRevParseCommand(repository: Repository) : GitCommand<List<String>>(repository) {

    // ----------------------------------------------------
    // region git command arguments

    private var commitId: String = Repository.HEAD

    private val short = GitNameValueParam<Int>("--short")

    private val symbolicFullName = GitNameValueParam<Unit>("--symbolic-full-name")

    private val abbrevRef = GitNameValueParam<AbbrevRefMode>("--abbrev-ref")

    private val symbolic = GitNameValueParam<Unit>("--symbolic")

    // endregion git command arguments

    fun commitId(id: String) = apply { commitId = id }
    fun short(len: Int? = null) = apply { short.set(len) }
    fun symbolicFullName() = apply { symbolicFullName.set() }
    fun abbrevRef(value: AbbrevRefMode) = apply { abbrevRef.set(value.takeIf { it != AbbrevRefMode.Auto }) }
    fun symbolic() = apply { symbolic.set() }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(short, abbrevRef, symbolicFullName, symbolic)
            add(commitId)
        }

        return GitRunner.execute("git --no-pager rev-parse $paramsBuilder")
            .await()
            .assertNoErrors()
            .readLines(GitRunner.StdOutput.Output)
    }

    enum class AbbrevRefMode(private val value: String) : GitParam {
        Auto("auto"),
        Loose("loose"),
        Strict("strict");

        override fun asQueryString(): String? = value
    }
}
