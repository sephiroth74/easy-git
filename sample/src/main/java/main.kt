import it.sephiroth.gradle.git.api.Git
import it.sephiroth.gradle.git.exception.GitExecutionException
import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.vo.LogCommit
import java.io.File
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException


private object GitUtil {

    @Throws(GitExecutionException::class)
    fun getLastGitVersion(git: Git): String {
        return git.repository.describe().tags().firstParent().abbrev(0).call().trim()
    }

    @Throws(GitExecutionException::class, ExecutionException::class)
    fun getCommits(git: Git, from: String, to: String, firstParentOnly: Boolean): List<LogCommit> {
        val logCommand = git.log.range(from, to)
        if (firstParentOnly) {
            logCommand.firstParent()
        }
        return logCommand.call().reversed()
    }

    fun isVersionCommit(commit: LogCommit): Boolean {
        return commit.subject?.matches(versionRe) == true
    }

    fun isMergeCommit(commit: LogCommit): Boolean {
        return commit.subject?.matches(mergeRe) == true
    }

    /**
     * If the commit object contains a version subject (like Version-1.0.0) then it's
     * considered a version commit and this method will extract the version value
     */
    fun getVersion(commit: LogCommit): String? {
        return versionRe.matchEntire(commit.sanitizedSubject ?: "")?.let { group ->
            if (group.groups.size == 2) group.groups[1]?.value
            else null
        }
    }

    data class Change(val commit: LogCommit, val ticketId: String, val ticketTitle: String?) {
        fun formattedString(): String = "${ticketId.uppercase(Locale.ROOT)}: $ticketTitle"

        companion object {

            fun of(commit: LogCommit): Change? {
                ticketRe.find(commit.body ?: "")?.let { group ->
                    if (group.groups.size == 4) {
                        val ticketId = group.groups[1]?.value
                        val ticketTitle = group.groups[3]?.value
                        if (null != ticketId) {
                            return Change(commit, ticketId, ticketTitle)
                        }
                    }
                }
                return null
            }
        }
    }


    @Throws(GitExecutionException::class, ExecutionException::class)
    fun generateReleaseNotesData(git: Git, from: String, to: String): ReleaseNotes {
        println("generating release-notes data from commits (from=$from, to=$to))")
        val t1 = System.currentTimeMillis()

        val commits1 = GitUtil.getCommits(git, from, to, false)
        val commits2 = GitUtil.getCommits(git, from, to, true)

        val t2 = System.currentTimeMillis()
        println("fetched 2 commits in ${t2 - t1}ms")

        val versionCommits = commits1.mapNotNull { commit ->
            if (GitUtil.isVersionCommit(commit)) {
                GitUtil.getVersion(commit)
            } else null
        }

        val t3 = System.currentTimeMillis()
        println("mapped commits in ${t3 - t2}ms")

        val mergeCommits = commits2.mapNotNull { commit ->
            if (GitUtil.isMergeCommit(commit)) {
                GitUtil.Change.of(commit)
            } else null
        }

        val t4 = System.currentTimeMillis()
        println("merged commits in ${t4 - t3}ms")

        println("Version commits: ")

        versionCommits.forEach {
            println("[\t$it")
        }

        println("Merge commits: ")
        mergeCommits.forEach {
            println("\t$it")
        }

        val sortedChanges = mergeCommits.sortedBy { it.ticketId }

        val t5 = System.currentTimeMillis()
        println("completed in ${t5 - t1}ms")

        return ReleaseNotes(versionCommits, sortedChanges)
    }

    private val ticketRe = "((TA|US|DE|TVUI-)[0-9]+)\\s*:\\s*(.*)".toRegex(RegexOption.IGNORE_CASE)
    private val mergeRe = "^merge branch '(.*)' into '(.*)'$".toRegex(RegexOption.IGNORE_CASE)
    private val versionRe = "^version[^\\w]+([0-9A-Za-z-.]+)\$".toRegex(RegexOption.IGNORE_CASE)
}

private data class ReleaseNotes(val includes: List<String>, val changes: List<GitUtil.Change>) {

    override fun toString(): String {
        val includesStr = if (includes.isEmpty()) "[]" else "[\n${includes.joinToString("\n") { "    - $it" }}\n  ]"
        val changesStr = if (changes.isEmpty()) "[]" else "[\n${changes.joinToString("\n") { "    - ${it.formattedString()}" }}\n  ]"
        return "ReleaseNotes(\n" +
                "  includes: $includesStr\n" +
                "  changes: $changesStr\n" +
                ")"
    }

    companion object {
        val EMPTY = ReleaseNotes(emptyList(), emptyList())
    }
}

fun main() {
    val currentDir = File(".")

    GitRunner.numThreads = 2
    val git = Git.open(currentDir)
    val branch = git.branch.name()

    println("version = ${Git.VERSION}, buildTime = ${Date(Git.Companion.BUILD_DATE)}")

    val fromGitVersion = GitUtil.getLastGitVersion(git).trim()
    val toGitVersion = git.repository.commitHash(false)

    println("fromGitVersion -> $fromGitVersion")
    println("toGitVersion -> $toGitVersion")

    val releaseNotes = try {
        GitUtil.generateReleaseNotesData(git, fromGitVersion, toGitVersion)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    println("releaseNotes = $releaseNotes")
}
