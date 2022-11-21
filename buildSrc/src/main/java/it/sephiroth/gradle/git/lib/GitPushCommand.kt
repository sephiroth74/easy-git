package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-push">git-push</a>
 */
class GitPushCommand(repo: Repository) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val porcelain = GitNameParam("--porcelain").set()

    private var type: PushType? = null
    private val force = GitNameParam("--force")
    private val delete = GitNameParam("--delete")
    private val prune = GitNameParam("--prune")
    private val verbose = GitNameParam("--verbose")
    private val setUpstream = GitNameParam("--set-upstream")
    private val repository = GitNameValueParam<String>("--repo")
    private val pushOption = GitNameValueParam<String>("--push-option")
    private val dryRun = GitNameParam("--dry-run")
    private var refSpec: String? = null

    // endregion git arguments

    fun force() = apply { force.set() }
    fun delete() = apply { delete.set() }
    fun prune() = apply { prune.set() }
    fun verbose() = apply { verbose.set() }
    fun setUpstream() = apply { setUpstream.set() }
    fun repository(value: String) = apply { repository.set(value) }
    fun dryRun() = apply { dryRun.set() }

    fun refSpec(name: String?) = apply {
        refSpec = name
        if (null == name) type = null
    }

    fun type(value: PushType?) = apply {
        type = value
        if (null != value) refSpec = null
    }


    override fun call(): String {
        val paramBuilder = ParamsBuilder().apply {
            add(type)
            add(porcelain)
            addAll(force, delete, prune, verbose, setUpstream, repository, pushOption, dryRun)
            add(refSpec)
        }

        val commands = mutableListOf("git", "push").apply {
            addAll(paramBuilder.toList())
        }

        return GitRunner
            .execute(commands)
            .await()
            .assertNoErrors()
            .readText() ?: ""

    }

    enum class PushType(private val value: String) : GitParam {
        All("--all"), Mirror("-mirror"), Tags("--tags");

        override fun asQueryString(): String = this.value
    }
}
