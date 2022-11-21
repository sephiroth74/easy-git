import it.sephiroth.gradle.git.api.Git
import it.sephiroth.gradle.git.exception.GitExecutionException
import it.sephiroth.gradle.git.lib.GitCommand
import it.sephiroth.gradle.git.lib.GitPushCommand.PushType
import it.sephiroth.gradle.git.lib.Repository
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date

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
    implementation("com.android.tools.build:gradle-api:7.3.1")
    implementation(gradleApi())

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        apiVersion = "1.7"
        languageVersion = "1.7"
        jvmTarget = "11"
    }
}

// test task //

tasks.create("testGit") {
    doLast {
        val git = Git.open(rootDir)
        logger.lifecycle("git => $git")
        logger.lifecycle("version = ${Git.VERSION}, buildTime = ${Date(Git.Companion.BUILD_DATE)}")


        logger.lifecycle("rev-list:     " + git.repository.revList().tags().maxCount(1).call())

        val penultimate = git.repository.revList().tags().maxCount(1).skip(1).call().first()

        logger.lifecycle("rev-list[-1]: " + penultimate)
        logger.lifecycle("describe:     " + git.repository.describe(penultimate).abbrev(0).tags().call())


        logger.lifecycle("git diff:")
        logger.lifecycle("" + git.diff.show().diffFilter("M").commits(GitCommand.RevisionRangeParam.head()).paths(File("easy-git/build.gradle.kt")).call())

        error("stop here")

        val commitHash = git.repository.resolve(Repository.HEAD).call().first()
        logger.lifecycle("commit hash => $commitHash")
        logger.lifecycle("branch name => " + git.branch.name())

        logger.lifecycle("rev-list")
        val lastCommit = git.repository.revList().maxCount(1).tags().call().first()
        logger.lifecycle("\trev-list => $lastCommit")
        logger.lifecycle("\tdescribe = " + git.repository.describe(lastCommit).abbrev(0).tags().call())

        logger.lifecycle("rev-list [previous]")
        val prevCommit = git.repository.revList().maxCount(1).skip(1).tags().call().first()
        logger.lifecycle("\trev-list => $prevCommit")
        logger.lifecycle("\tdescribe = " + git.repository.describe(prevCommit).abbrev(0).tags().call())

        logger.lifecycle("git log --pretty=oneline 735e044..HEAD")

        git.log.range("735e044", "HEAD").call().forEach { commit ->
            logger.lifecycle("\tLogCommit{")
            logger.lifecycle("\t\tcommit:  ${commit.commitId}")
            logger.lifecycle("\t\ttree:    ${commit.tree}")
            logger.lifecycle("\t\tsubject: ${commit.subject}")
            logger.lifecycle("\t\tauthor:  ${commit.author}")
            logger.lifecycle("\t\ttags:    ${commit.tags}")
            logger.lifecycle("\t\tbody:    ${commit.body}")
            logger.lifecycle("\t}")
        }

        logger.lifecycle("git add: ")
        logger.lifecycle(
            "\t" + git.repository
                .add(
                    File("buildSrc/src/main/java/it/sephiroth/gradle/git/lib/GitAddCommand.kt"),
                    File("buildSrc/src/main/java/it/sephiroth/gradle/git/api/Git.kt")
                )
                .dryRun().ignoreMissing().ignoreErrors().call()
        )

        logger.lifecycle("list staged files: ")
        git.diff.fileList().cached().call().forEach { file ->
            logger.lifecycle("\t$file")
        }


        logger.lifecycle("git commit: ")
        logger.lifecycle(
            "\t" + git.repository.commit()
                .dryRun()
                .message("This is a test message")
                .quiet()
                .all()
                .call()
        )

        logger.lifecycle("Deleting branch 'test.branch'")
        try {
            logger.lifecycle("\t" + git.branch.delete("test.branch").force().call())
        } catch (e: GitExecutionException) {
            logger.warn("\t" + e.message)
        }

        logger.lifecycle("Creating a new test tag")
        logger.lifecycle("\t" + git.tag.add("test.tag.01").message("test tag message").force().call())

        logger.lifecycle("git push")
        logger.lifecycle("\t" + git.repository.push().prune().type(PushType.Tags).verbose().call())
    }
}
