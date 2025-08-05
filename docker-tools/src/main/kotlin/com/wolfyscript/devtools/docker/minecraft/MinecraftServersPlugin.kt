package com.wolfyscript.devtools.docker.minecraft

import com.wolfyscript.devtools.docker.Constants
import com.wolfyscript.devtools.docker.run.DockerRunExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class MinecraftServersPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create(
            Constants.EXT_MINECRAFT_SERVERS_NAME,
            MinecraftServersExtension::class,
            target.objects,
            target
        )

        target.extensions.create<DockerRunExtension>(
            Constants.EXT_MINECRAFT_RUN_NAME,
            target.objects,
            target
        ).apply {
            name.convention("test_mc_server")
            image.convention("itzg/minecraft-server")
            env.convention(buildMap {
                this["EULA"] = "TRUE" // Enable by default. setting it to false makes no sense
                this["USE_AIKAR_FLAGS"] = "TRUE" // Recommended, so gonna enable it by default
            })
            ports.convention(listOf("25565:25565"))
            arguments.convention(listOf("-it")) // Allow for console interactivity with 'docker attach'
        }

    }

}