package it.sephiroth.gradle.git.lib

/**
 * @see <a href="https://git-scm.com/docs/git-remote">git remote</a>
 */
class GitRemoteCommand(repo: Repository) : GitCommand<String>(repo) {

    // ----------------------------------------------------
    // region git arguments

    private val remove = GitNameValueParam<String>("--remove")

    // endregion git arguments

    override fun call(): String {
    }
}
