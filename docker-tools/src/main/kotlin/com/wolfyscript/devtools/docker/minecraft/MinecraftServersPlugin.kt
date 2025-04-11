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

class MinecraftServersPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create<MinecraftServersExtension>("minecraftServers")

        val defaultMCDockerRun = target.extensions.create<DockerRunExtension>("minecraftDockerRun").apply {
            name.set("test_mc_server");
            image.set("itzg/minecraft-server");
            daemonize.set(true);
            clean.set(true);

            ports("25565:25565")

            env(buildMap {
                this["EULA"] = "TRUE" // Enable by default. setting it to false makes no sense
                this["USE_AIKAR_FLAGS"] = "TRUE" // Recommended, so gonna enable it by default
            })

            arguments("-it") // Allow for console interactivity with 'docker attach'
        }

        target.afterEvaluate {

            val serversExtension: MinecraftServersExtension = target.extensions.getByType<MinecraftServersExtension>()
            val directory : Directory = serversExtension.serversDir.getOrElse(target.layout.projectDirectory.dir("test_servers"))
            val libDir: Directory = serversExtension.libDir.orElse(target.layout.buildDirectory.dir("libs")).getOrElse(target.layout.projectDirectory.dir("/build/libs"))
            val libName: Property<String> = serversExtension.libName

            for (serverEntry in serversExtension.servers) {
                val serverName: String = serverEntry.name
                val serverPath: String = serverEntry.serverDir.getOrElse(directory.dir(serverName)).asFile.path

                val copyTask = target.task<Copy>("${serverName}_copy") {
                    dependsOn(target.tasks.getByName("jar"))
                    dependsOn("shadowJar")

                    val file: RegularFile = serverEntry.libDir.getOrElse(libDir).file(serverEntry.libName.getOrElse(libName.getOrElse("${target.project.name}-${target.project.version}.jar")))
                    println("Configure Copy: ${file.asFile.path} to server $serverPath/plugins")
                    from(file)
                    rename(".*", serverEntry.destFileName.getOrElse("${target.project.name}.jar"))
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
                    if (serverEntry.imageVersion.isPresent) {
                        image.set("${image.get()}:${serverEntry.imageVersion.get()}")
                    }
                    if (serverEntry.version.isPresent) {
                        customEnv["VERSION"] = serverEntry.version.get()
                    }
                    if (serverEntry.type.isPresent) {
                        customEnv["TYPE"] = serverEntry.type.get()
                    }
                    if (serverEntry.extraEnv.isPresent) {
                        customEnv.putAll(serverEntry.extraEnv.get())
                    }
                    env.set(customEnv)

                    val customVolumes = volumes.get().toMutableMap()
                    customVolumes[serverPath] = "/data"
                    volumes.set(customVolumes)
                }

            }

        }

    }

}