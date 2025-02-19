import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.util.regex.Pattern

val rootProperties = Properties().also {
    File(rootDir.parentFile, "gradle.properties").inputStream().use { stream ->
        it.load(stream)
    }
}

println(rootProperties.keys)

val projectVersion: String by rootProperties

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    groovy
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/releases") }
    maven { url = uri("https://repo1.maven.org/maven2") }
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

project.version = projectVersion

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.android.tools.build:gradle-api:8.8.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.1.10")
    implementation(gradleApi())
}

tasks.create("updateVersion") {
    doLast {
        logger.lifecycle("updating Git.VERSION with ${project.version}")
        val inputFile =
            File("${project.projectDir}/src/main/java/it/sephiroth/gradle/git/api/Git.kt")

        var newFileText: String? = null

        inputFile.reader().use { reader ->
            val fileText = reader.readText()

            val buildDateRegExp = Pattern.compile(
                "const\\s+val\\s+BUILD_DATE\\s*:\\s*Long\\s*=\\s*[0-9]+",
                Pattern.MULTILINE or Pattern.DOTALL
            )

            val versionRegExp = Pattern.compile(
                "const val VERSION:\\s*String\\s*=\\s*\"([0-9]+.[0-9]+.[^\"]+)\"",
                Pattern.MULTILINE or Pattern.DOTALL
            )

            newFileText = fileText.replace(
                versionRegExp.toRegex(),
                "const val VERSION: String = \"${project.version}\""
            )

            newFileText = newFileText?.replace(
                buildDateRegExp.toRegex(),
                "const val BUILD_DATE: Long = ${System.currentTimeMillis()}"
            )
        }

        newFileText?.takeIf { !it.isBlank() }?.let { text ->
            inputFile.outputStream().writer().use {
                println("writing new text with size -> ${text.length}")
                it.write(text)
                it.flush()
            }
        } ?: run {
            logger.warn("Could not update Git.VERSION")
        }
    }
}

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
