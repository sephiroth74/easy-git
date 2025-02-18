import it.sephiroth.gradle.git.api.Git
import it.sephiroth.gradle.git.exception.GitExecutionException
import it.sephiroth.gradle.git.executor.GitRunner
import it.sephiroth.gradle.git.lib.GitCommand
import it.sephiroth.gradle.git.lib.GitPushCommand.PushType
import it.sephiroth.gradle.git.lib.Repository
import it.sephiroth.gradle.git.vo.LogCommit
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException

plugins {
    `kotlin-dsl`
    `java-library`
    groovy
    signing
    java
    idea
    `maven-publish`
}

val SONATYPE_RELEASE_URL: String by rootProject
val SONATYPE_SNAPSHOT_URL: String by rootProject

val projectGroupId: String by rootProject
val projectVersion: String by rootProject
val projectName: String by rootProject
val artifactId: String by rootProject

val pomDescription: String by rootProject
val pomLicenseUrl: String by rootProject
val pomLicenseName: String by rootProject
val pomDeveloperId: String by rootProject
val pomDeveloperName: String by rootProject
val pomDeveloperEmail: String by rootProject

val scmUrl: String by rootProject
val scmDeveloperConnection: String by rootProject
val scmConnection: String by rootProject

project.version = projectVersion
project.group = projectGroupId


tasks {
    artifacts {
        archives(jar)
    }
}

gradlePlugin {
    plugins {
        create("easy-git") {
            id = "it.sephiroth.gradle.easy-git"
            implementationClass = "it.sephiroth.gradle.git.EasyGitPlugin"
            displayName = "gradle easy git"
            description = "easy git for gradle scripts"
            version = version
        }
    }
}

if (project.hasProperty("sonatypeUsername")
    && project.hasProperty("sonatypePassword")
    && project.hasProperty("SONATYPE_RELEASE_URL")
    && project.hasProperty("SONATYPE_SNAPSHOT_URL")
) {
    val publishingUrl =
        if (projectVersion.endsWith("-SNAPSHOT")) SONATYPE_SNAPSHOT_URL else SONATYPE_RELEASE_URL

    logger.lifecycle("project version: $projectGroupId:$artifactId:${version}")
    logger.lifecycle("publishing url = $publishingUrl")

    publishing {
        publications {
            create<MavenPublication>("pluginMaven") {
                groupId = projectGroupId
                version = version

                pom {
                    groupId = projectGroupId
                    version = version

                    description.set(pomDescription)
                    url.set(scmUrl)
                    name.set(project.name)

                    licenses {
                        license {
                            name.set(pomLicenseName)
                            url.set(pomLicenseUrl)
                        }
                    }

                    scm {
                        url.set(scmUrl)
                        connection.set(scmConnection)
                        developerConnection.set(scmDeveloperConnection)
                        tag.set("${project.name}-${project.version}")
                    }

                    developers {
                        developer {
                            id.set(pomDeveloperId)
                            name.set(pomDeveloperName)
                            email.set(pomDeveloperEmail)
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                url = uri(publishingUrl)
                credentials {
                    val sonatypeUsername: String by project
                    val sonatypePassword: String by project

                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }

        signing {
            sign(publishing.publications["pluginMaven"])
        }

        tasks.withType<Sign> {
            onlyIf { !projectVersion.endsWith("-SNAPSHOT") }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir(file("../buildSrc/src/main/java"))
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.android.tools.build:gradle-api:8.8.0")
    implementation(gradleApi())

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        apiVersion = "2.1"
        languageVersion = "2.1"
        jvmTarget = "21"
    }
}

// test task //


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

@Throws(GitExecutionException::class, ExecutionException::class)
private fun generateReleaseNotesData(git: Git, from: String, to: String): ReleaseNotes {
    logger.info("[$name] generating release-notes data from commits (from=$from, to=$to))")
    val t1 = System.currentTimeMillis()

    val commits1 = GitUtil.getCommits(git, from, to, false)
    val commits2 = GitUtil.getCommits(git, from, to, true)

    val t2 = System.currentTimeMillis()
    logger.info("[$name] fetched 2 commits in ${t2 - t1}ms")

    val versionCommits = commits1.mapNotNull { commit ->
        if (GitUtil.isVersionCommit(commit)) {
            GitUtil.getVersion(commit)
        } else null
    }

    val t3 = System.currentTimeMillis()
    logger.info("[$name] mapped commits in ${t3 - t2}ms")

    val mergeCommits = commits2.mapNotNull { commit ->
        if (GitUtil.isMergeCommit(commit)) {
            GitUtil.Change.of(commit)
        } else null
    }

    val t4 = System.currentTimeMillis()
    logger.info("[$name] merged commits in ${t4 - t3}ms")

    if (logger.isDebugEnabled) {
        logger.info("[$name] Version commits: ")

        versionCommits.forEach {
            logger.info("[$name] \t$it")
        }

        logger.info("Merge commits: ")
        mergeCommits.forEach {
            logger.info("[$name] \t$it")
        }
    }

    val sortedChanges = mergeCommits.sortedBy { it.ticketId }

    val t5 = System.currentTimeMillis()
    logger.info("[$name] completed in ${t5 - t1}ms")

    return ReleaseNotes(versionCommits, sortedChanges)
}

tasks.create("testGit") {
    doLast {
        GitRunner.numThreads = 2
        val git = Git.open(rootDir)
        val branch = git.branch.name()

        logger.lifecycle("git => $git")
        logger.lifecycle("version = ${Git.VERSION}, buildTime = ${Date(Git.Companion.BUILD_DATE)}")

        val fromGitVersion = GitUtil.getLastGitVersion(git).trim()
        val toGitVersion = git.repository.commitHash(false)

        logger.lifecycle("fromGitVersion -> $fromGitVersion")
        logger.lifecycle("toGitVersion -> $toGitVersion")

        val releaseNotes = try {
            generateReleaseNotesData(git, fromGitVersion, toGitVersion)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        logger.lifecycle("releaseNotes = $releaseNotes")

    }
}
