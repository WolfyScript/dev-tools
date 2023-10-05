package com.wolfyscript.devtools.buildtools

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.nio.channels.Channels

abstract class BuildToolsUpdateTask : DefaultTask() {

    companion object {
        private const val BUILD_TOOLS_URL: String = "https://hub.spigotmc.org/jenkins/job/BuildTools"
        const val BUILD_TOOLS_JAR_URL: String = "${BUILD_TOOLS_URL}/lastSuccessfulBuild/artifact/target/BuildTools.jar"
        const val BUILD_TOOLS_API_URL: String = "${BUILD_TOOLS_URL}/lastBuild/api/json"
    }

    @get:OutputFile
    @get:Optional
    val buildToolsJar: RegularFileProperty = this.project.objects.fileProperty().convention(this.project.layout.projectDirectory.dir("buildtools").file("BuildTools.jar"))

    @get:OutputDirectory
    @get:Optional
    val buildToolsDir: DirectoryProperty = this.project.objects.directoryProperty().convention(this.project.layout.projectDirectory.dir("buildtools"))

    @TaskAction
    private fun update() {
        // Check for existing buildtools
        val buildToolsJar: File = buildToolsJar.get().asFile
        val dir = buildToolsDir.get().asFile
        dir.mkdirs()

        if (!buildToolsJar.exists()) {
            this.logger.info("Could not find existing BuildTools jar. Downloading...")
            fetchInfo().fold(onSuccess = {
                download(URL(BUILD_TOOLS_JAR_URL), buildToolsJar)
            }, onFailure = {
                error("Failed to fetch latest BuildTools version! Cannot find existing BuildTools jar as fallback!")
            })
        } else if (System.currentTimeMillis() - buildToolsJar.lastModified() > 1000 * 60 * 60 * 24) {
            fetchInfo().fold(onSuccess = {
                if (it.timestamp > System.currentTimeMillis()) {
                    // Only update if a new build is available since last update
                    download(URL(BUILD_TOOLS_JAR_URL), buildToolsJar)
                } else {
                    // Mark file as modified as it is up-to-date
                    buildToolsJar.setLastModified(System.currentTimeMillis())
                }
            }, onFailure = {
                this.logger.warn("Failed to fetch latest BuildTools version. Use current version of BuildTools!", it)
            })
        } else {
            this.logger.info("Existing BuildTools jar is up to date!")
        }
    }

    private fun fetchInfo(): Result<Response> {
        return try {
            val mapper = JsonMapper.builder().addModule(kotlinModule()).build()
            Result.success(mapper.readValue<Response>(URL(BUILD_TOOLS_API_URL)))
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }

    private fun download(remote: URL, destination: File) {
        this.logger.info("Start downloading ${destination.path} from $remote")
        destination.parentFile.mkdirs()
        remote.openStream().use { stream ->
            destination.outputStream().use { out ->
                out.channel.transferFrom(Channels.newChannel(stream), 0, Long.MAX_VALUE)
            }
        }
        this.logger.info("Done Downloading")
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Response(val timestamp: Long)

}