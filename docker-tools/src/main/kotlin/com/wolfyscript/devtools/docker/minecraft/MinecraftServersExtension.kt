package com.wolfyscript.devtools.docker.minecraft

import com.wolfyscript.devtools.docker.Constants
import com.wolfyscript.devtools.docker.run.DockerRunExtension
import com.wolfyscript.devtools.docker.run.DockerRunTask
import com.wolfyscript.devtools.docker.run.DockerStopTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import kotlin.collections.toMutableMap

abstract class MinecraftServersExtension(objects: ObjectFactory, project: Project) {

    val servers : NamedDomainObjectContainer<ServerEntry> = objects.domainObjectContainer(ServerEntry::class.java)

    init {
        servers.all {
            serverDir.convention(serversDir.dir(name))
            libDir.convention(this@MinecraftServersExtension.libDir)
            libName.convention(this@MinecraftServersExtension.libName)
            destFileName.convention("${project.name}.jar")
            destPath.convention("plugins")
        }
        servers.all {
            val serverPath: String = serverDir.get().asFile.path
            val runExtension = project.extensions.getByName<DockerRunExtension>(Constants.EXT_MINECRAFT_RUN_NAME)

            val copyTask = project.tasks.register<Copy>("${name}_copy") {
                group = Constants.TASK_MINECRAFT_RUN_GROUP_NAME

                dependsOn(project.tasks.getByName("jar"))

                if (project.tasks.findByName("shadowJar") != null) {
                    dependsOn("shadowJar")
                }

                val file: RegularFile = libDir.get().file(libName.get())
                println("copy: ${file.asFile.path} to server $serverPath/${destPath.get()}/")
                from(file)
                rename(".*", destFileName.get())
                into("$serverPath/${destPath.get()}/")
            }

            val stopTask = project.tasks.register<DockerStopTask>("${name}_stop") {
                group = Constants.TASK_MINECRAFT_RUN_GROUP_NAME

                name.convention("${runExtension.name.get()}_${this@all.name}")
            }

            project.tasks.register<DockerRunTask>("${name}_run") {
                group = Constants.TASK_MINECRAFT_RUN_GROUP_NAME
                dependsOn(copyTask)
                dependsOn(stopTask)

                applyExtension(runExtension)

                name.convention("${runExtension.name.get()}_${this@all.name}")
                project.mkdir(serverPath)

                // merge all the ports instead of replacing them (convention)
                ports.addAll(runExtension.ports)
                ports.addAll(this@all.ports)

                if (imageVersion.isPresent) {
                    image.set("${image.get()}:${imageVersion.get()}")
                }

                val customEnv = env.get().toMutableMap()
                if (version.isPresent) {
                    customEnv["VERSION"] = version.get()
                }
                if (type.isPresent) {
                    customEnv["TYPE"] = type.get()
                }
                if (extraEnv.isPresent) {
                    customEnv.putAll(extraEnv.get())
                }
                env.set(customEnv)

                val customVolumes = volumes.get().toMutableMap()
                customVolumes[serverPath] = "/data"
                volumes.set(customVolumes)
            }
        }
    }

    @get:Optional
    val serversDir : DirectoryProperty = objects.directoryProperty().convention(project.layout.projectDirectory.dir("test_servers"))

    val libName: Property<String> = objects.property(String::class.java).convention("${project.name}-${project.version}.jar")

    @get:Optional
    val libDir: DirectoryProperty = objects.directoryProperty().convention(project.layout.buildDirectory.dir("libs"))

}