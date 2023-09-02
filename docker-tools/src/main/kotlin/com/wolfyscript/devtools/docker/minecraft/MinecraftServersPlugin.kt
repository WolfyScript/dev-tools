package com.wolfyscript.devtools.docker.minecraft

import com.wolfyscript.devtools.docker.run.DockerRunExtension
import com.wolfyscript.devtools.docker.run.DockerRunTask
import com.wolfyscript.devtools.docker.run.DockerStopTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.task
import java.io.File
import java.util.*

class MinecraftServersPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create<MinecraftServersExtension>("minecraftServers")

        val defaultMCDockerRun = target.extensions.create<DockerRunExtension>("minecraftDockerRun").apply {
            name.set("test_mc_server");
            image.set("itzg/minecraft-server");
            daemonize.set(true);
            clean.set(true);

            ports("25565:25565")

            val serverEnv = mutableMapOf<String, String>().apply {
                this["TYPE"] = "SPIGOT"
                this["VERSION"] = "1.20.1"
                this["GUI"] = "FALSE"
                this["EULA"] = "TRUE"
                this["MEMORY"] = "2G"
                this["USE_AIKAR_FLAGS"] = "TRUE"
            }
            env(serverEnv)

            arguments("-it") // Allow for console interactivity with 'docker attach'
        }

        target.afterEvaluate {

            val serversExtension: MinecraftServersExtension = target.extensions.getByType<MinecraftServersExtension>()
            val directory : Directory = serversExtension.serversDir.getOrElse(target.layout.projectDirectory.dir("test_servers"))
            val libDir: Directory = serversExtension.libDir.orElse(target.layout.buildDirectory.dir("libs")).getOrElse(target.layout.projectDirectory.dir("/build/libs"))
            val libName: Property<String> = serversExtension.libName

            for (serverEntry in serversExtension.servers) {
                val version = serverEntry.version.get()
                val serverName = serverEntry.name
                val serverPath = serverEntry.serverDir.orElse(directory.dir(serverName))

                val copyTask = target.task<Copy>("${serverName}_copy") {
                    dependsOn("shadowJar")

                    val file: RegularFile = serverEntry.libDir.getOrElse(libDir).file(serverEntry.libName.getOrElse(libName.getOrElse("${target.project.name}-${target.project.version}.jar")))
                    println("Copy file ${file.asFile.path} to server: $serverPath/plugins")
                    from(file)
                    into("$serverPath/plugins")
                }

                val stopTask = target.task<DockerStopTask>("${serverName}_stop") {
                    applyExtension(defaultMCDockerRun)
                    name.set("${name.get()}_$serverName")
                }

                target.task<DockerRunTask>("${serverName}_run") {
                    dependsOn(copyTask)
                    dependsOn(stopTask)

                    applyExtension(defaultMCDockerRun)
                    name.set("${name.get()}_$serverName")

                    println(serverPath)
                    mkdir(serverPath)

                    ports.set(serverEntry.ports.get())

                    val customEnv = env.get().toMutableMap()
                    customEnv["VERSION"] = version
                    env.set(customEnv)

                    val customVolumes = volumes.get().toMutableMap()
                    customVolumes[serverPath] = "/data"
                    volumes.set(customVolumes)
                }

            }

        }

    }

}