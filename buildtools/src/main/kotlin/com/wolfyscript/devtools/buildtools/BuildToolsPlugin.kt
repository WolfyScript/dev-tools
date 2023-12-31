package com.wolfyscript.devtools.buildtools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class BuildToolsPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val extension = target.extensions.create<BuildToolsExtension>("buildTools").apply {
            buildToolsDir.convention(target.layout.projectDirectory.dir("buildtools"))
            buildToolsJar.convention(target.objects.fileProperty().convention(buildToolsDir.file("BuildTools.jar")))
        }

        target.tasks.create<BuildToolsUpdateTask>("prepareBuildTools") {
            buildToolsJar.convention(extension.buildToolsJar)
            buildToolsDir.convention(extension.buildToolsDir)
        }

        target.tasks.create<BuildToolsInstallTask>("prepareNMS") {
            buildToolsDir.convention(extension.buildToolsDir)
            buildToolsJar.convention(extension.buildToolsJar)
            minecraftVersion.convention(extension.minecraftVersion)
        }

    }

}