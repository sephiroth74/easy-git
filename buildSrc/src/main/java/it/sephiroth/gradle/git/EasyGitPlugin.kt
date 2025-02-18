package it.sephiroth.gradle.git

import org.gradle.api.Plugin
import org.gradle.api.Project

class EasyGitPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.logger.quiet("EasyGitPlugin applied to project ${target.name}")
    }

}
