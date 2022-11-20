// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    dependencies {
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.7.20-1.0.7")
//        classpath("it.sephiroth.gradle.plugin:easy-git:1.0.2-SNAPSHOT")
    }
}

plugins {
    id("com.android.application") version "7.3.0" apply false
    id("com.android.library") version "7.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false

    kotlin("jvm") version "1.7.21"


}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}




