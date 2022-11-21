package it.sephiroth.gradle.git.lib

import it.sephiroth.gradle.git.executor.GitRunner

/**
 * @see <a href="https://git-scm.com/docs/git-for-each-ref">git for-each-ref</a>
 */
class GitBranchListCommand(repository: Repository) : GitCommand<List<String>>(repository) {
    // ----------------------------------------------------
    // region git arguments

    private var branchMode = BranchMode.Local
    private val format: GitNameValueParam<String> = GitNameValueParam("--format", "%(refname:short)")

    // endregion git arguments

    fun mode(mode: BranchMode) = apply { branchMode = mode }

    fun format(value: String) = apply { format.set(value) }

    override fun call(): List<String> {
        val paramsBuilder = ParamsBuilder().apply {
            addAll(format)

            when (branchMode) {
                BranchMode.Local -> add(Repository.REF_HEADS)
                BranchMode.Remotes -> add(Repository.REF_REMOTES)
                BranchMode.All -> {
                    add(Repository.REF_HEADS)
                    add(Repository.REF_REMOTES)
                }
            }
        }

        val cmd = "git --no-pager for-each-ref $paramsBuilder"
        return GitRunner.execute(cmd, repo.repoDir).await().assertNoErrors().readLines(GitRunner.StdOutput.Output)
    }

    enum class BranchMode {
        All, Remotes, Local
    }
}
