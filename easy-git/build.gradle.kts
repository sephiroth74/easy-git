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

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
    useJUnitPlatform()
}


publishing {
    repositories {
        mavenLocal()

        if (localProperties.containsKey("publishingReleaseUrl") && localProperties.containsKey("publishingSnapshotUrl")) {
            maven(ProjectUtil().artifactory(project))
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

class ProjectUtil {
    fun artifactory(project: Project): Action<in MavenArtifactRepository> {
        val isSnapshot = project.version.toString().endsWith("SNAPSHOT")

        val publishingReleaseUrl: String = localProperties.getProperty("publishingReleaseUrl")
        val publishingSnapshotUrl: String = localProperties.getProperty("publishingSnapshotUrl")
        val publishingUrl = if (!isSnapshot) publishingReleaseUrl else publishingSnapshotUrl
        val repoUsername = project.findProperty("artifactoryUser") as String
        val repoPassword = project.findProperty("artifactoryPassword") as String

        return Action {
            project.logger.info("publishing URL for ${project.name} = $publishingUrl")
            name = "ArtifactoryPublish"
            url = URI.create(publishingUrl)
            credentials {
                username = repoUsername
                password = repoPassword
            }
        }
    }

}