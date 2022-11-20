// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("com.android.tools.build:gradle-api:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.7.20")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.7.20-1.0.7")
        classpath("it.sephiroth.gradle.plugin:easy-git:1.0.2-SNAPSHOT")
    }
}

plugins {
    id("com.android.application") version ("7.3.1") apply (false)
    id("org.jetbrains.kotlin.android") version ("1.7.20") apply false

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}




