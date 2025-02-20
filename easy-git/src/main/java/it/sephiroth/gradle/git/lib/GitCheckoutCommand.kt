package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-checkout">git checkout</a>
 */
class GitCheckoutCommand(repo: Repository) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private var force = GitNameParam("-f")
    private val merge = GitNameParam("--merge")
    private val detach = GitNameParam("--detach")
    private val track = GitNameParam("--track")
    private val noTrack = GitNameParam("--no-track")

    private val autoCreate = GitNameParam("-b")
    private val autoCreateOrReset = GitNameParam("-B")

    private val recurseSubmodules = GitNameParam("--recurse-submodules")
    private val noRecurseSubmodules = GitNameParam("--no-recurse-submodules")

    private var localPathSpec: String? = null
    private var remotePathSpec: String? = null

    // endregion git arguments

    fun force() = apply { force.set() }
    fun merge() = apply { merge.set() }
    fun detach() = apply { detach.set() }
    fun track() = apply { track.set(); noTrack.unset() }
    fun noTrack() = apply { noTrack.set(); track.unset() }
    fun recurseSubmodules() = apply { recurseSubmodules.set(); noRecurseSubmodules.unset() }
    fun noRecurseSubmodules() = apply { noRecurseSubmodules.set(); recurseSubmodules.unset() }

    fun autoCreate() = apply { autoCreate.set(); autoCreateOrReset.unset() }
    fun autoCreateOrReset() = apply { autoCreateOrReset.set(); autoCreate.unset() }

    fun localPath(name: String) = apply { localPathSpec = name }
    fun remotePath(name: String) = apply { remotePathSpec = name }

    override fun call(): String {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(force, recurseSubmodules, noRecurseSubmodules, merge, detach)

            // -b or -B
            addAll(autoCreate, autoCreateOrReset)
            add(localPathSpec)

            // --track
            addAll(track, noTrack)

            // --remote
            add(remotePathSpec)
        }

        val cmd = "git checkout --no-progress $paramsBuilder"
        return GitRunner.execute(cmd, repo.repoDir)
            .await()
            .assertNoErrors()
            .readText(GitRunner.StdOutput.Output) ?: ""
    }

}
