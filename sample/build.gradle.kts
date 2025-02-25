
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.1.10")
    }
}

plugins {
    `kotlin-dsl`
    groovy
    java
}

dependencies {
    implementation(project(":easy-git"))
}

