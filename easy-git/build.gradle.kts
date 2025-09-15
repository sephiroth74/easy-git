import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URI
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException

plugins {
    `kotlin-dsl`
    java
    signing
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
val localProperties = gradleLocalProperties(rootDir, providers)

project.version = projectVersion
project.group = projectGroupId


dependencies {
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.android.tools.build:gradle-api:8.13.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.1.10")
    implementation(gradleApi())
}


java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
    withJavadocJar()
}


kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}



if (project.hasProperty("SONATYPE_TOKEN_USER")
    && project.hasProperty("SONATYPE_TOKEN_PASSWORD")
    && project.hasProperty("SONATYPE_RELEASE_URL")
    && project.hasProperty("SONATYPE_SNAPSHOT_URL")
) {
    val publishingUrl =
        if (projectVersion.endsWith("-SNAPSHOT")) SONATYPE_SNAPSHOT_URL else SONATYPE_RELEASE_URL

    logger.lifecycle("project version: $projectGroupId:$artifactId:${version}")
    logger.lifecycle("publishing url = $publishingUrl")

    publishing {
        repositories {
            maven {
                name = "sonatype"
                url = uri(publishingUrl)
                credentials {
                    val SONATYPE_TOKEN_USER: String by project
                    val SONATYPE_TOKEN_PASSWORD: String by project

                    username = SONATYPE_TOKEN_USER
                    password = SONATYPE_TOKEN_PASSWORD
                }
            }


            if (localProperties.containsKey("publishingReleaseUrl") && localProperties.containsKey("publishingSnapshotUrl")) {
                val publishingReleaseUrl: String = localProperties.getProperty("publishingReleaseUrl")
                val publishingSnapshotUrl: String = localProperties.getProperty("publishingSnapshotUrl")
                maven(ProjectUtil.artifactory(project, publishingReleaseUrl, publishingSnapshotUrl))
            }


        }

        publications {
            create<MavenPublication>("pluginMaven") {

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

        signing {
            sign(publishing.publications["pluginMaven"])
        }

        tasks.withType<Sign> {
            onlyIf { !projectVersion.endsWith("-SNAPSHOT") }
        }
    }
}

object ProjectUtil {

    fun artifactory(project: Project, publishingReleaseUrl: String, publishingSnapshotUrl: String): Action<in MavenArtifactRepository> {
        val isSnapshot = project.version.toString().endsWith("SNAPSHOT")
        val publishingUrl = if (!isSnapshot) publishingReleaseUrl else publishingSnapshotUrl

        val repoUsername = project.findProperty("artifactoryUser") as String
        val repoPassword = project.findProperty("artifactoryPassword") as String

        println("publishing URL for ${project.name} = $publishingUrl")

        return Action {
            name = "artifactory"
            url = URI.create(publishingUrl)



            credentials {
                username = repoUsername
                password = repoPassword
            }
        }
    }
}

