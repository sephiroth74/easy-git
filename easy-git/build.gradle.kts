import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    signing
    java
    idea
    `maven-publish`
}

val SONATYPE_RELEASE_URL: String by project
val SONATYPE_SNAPSHOT_URL: String by project

val projectGroupId: String by project
val projectVersion: String by project
val projectName: String by project
val artifactId: String by project

val pomDescription: String by project
val pomLicenseUrl: String by project
val pomLicenseName: String by project
val pomDeveloperId: String by project
val pomDeveloperName: String by project
val pomDeveloperEmail: String by project

val scmUrl: String by project
val scmDeveloperConnection: String by project
val scmConnection: String by project

project.version = projectVersion
project.group = projectGroupId


tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
    }

    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")
        dependsOn.add(javadoc)
        from(javadoc)
    }

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

