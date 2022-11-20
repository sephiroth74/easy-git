package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-fetch">git fetch</a>
 */
class GitFetchCommand(repo: Repository) : GitCommand<List<String>>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val all = GitNameParam("--all")
    private val atomic = GitNameParam("--atomic")
    private val depth = GitNameValueParam<Int>("--depth")
    private val unshallow = GitNameParam("--unshallow")
    private val updateShallow = GitNameParam("--update-shallow")
    private val dryRun = GitNameParam("--dry-run")
    private val force = GitNameParam("--force")
    private val keep = GitNameParam("--keep")
    private val prune = GitNameParam("--prune")
    private val pruneTags = GitNameParam("--prune-tags")
    private val noTags = GitNameParam("--no-tags")
    private val refetch = GitNameParam("--refetch")
    private val tags = GitNameParam("--tags")
    private val recurseSubmodules = GitNameValueParam<RecurseSubmoduleMode>("--recurse-submodules")
    private val jobs = GitNameValueParam<Int>("--jobs")
    private val quiet = GitNameParam("--quiet")
    private val verbose = GitNameParam("--verbose")
    private val showForcedUpdates = GitNameParam("--show-forced-updates")
    private val noShowForcedUpdates = GitNameParam("--no-show-forced-updates")
    private val ipv4 = GitNameParam("--ipv4")
    private val ipv6 = GitNameParam("--ipv6")
    private var repository: String? = null

    // endregion git arguments

    fun all() = apply { all.set() }
    fun atomic() = apply { atomic.set() }
    fun depth(value: Int) = apply { depth.set(value) }
    fun unshallow() = apply { unshallow.set() }
    fun updateShallow() = apply { updateShallow.set() }
    fun dryRun() = apply { dryRun.set() }
    fun force() = apply { force.set() }
    fun keep() = apply { keep.set() }
    fun prune() = apply { prune.set() }
    fun pruneTags() = apply { pruneTags.set() }
    fun noTags() = apply { noTags.set() }
    fun refetch() = apply { refetch.set() }
    fun tags() = apply { tags.set() }
    fun recurseSubmodules(value: RecurseSubmoduleMode?) = apply { recurseSubmodules.set(value) }
    fun jobs(value: Int) = apply { jobs.set(value) }
    fun quiet() = apply { quiet.set() }
    fun verbose() = apply { verbose.set() }
    fun showForcedUpdates() = apply { showForcedUpdates.set() }
    fun noShowForcedUpdates() = apply { noShowForcedUpdates.set() }
    fun ipv4() = apply { ipv4.set() }
    fun ipv6() = apply { ipv6.set() }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(
                atomic,
                depth,
                unshallow,
                updateShallow,
                dryRun,
                force,
                keep,
                prune,
                pruneTags,
                noTags,
                refetch,
                recurseSubmodules,
                jobs,
                quiet,
                verbose,
                showForcedUpdates,
                noShowForcedUpdates,
                ipv4,
                ipv6,
                tags,
                all
            )
            add(repository)

        }

        val cmd = "git fetch --progress $paramsBuilder"

        return GitRunner.execute(cmd)
            .await()
            .assertNoErrors()
            .readLines(GitRunner.StdOutput.Error)
    }

    enum class RecurseSubmoduleMode(private val value: String) : GitParam {
        Yes("yes"), No("no"), OnDemand("on-demand");

        override fun asQueryString(): String? = value
    }
}
