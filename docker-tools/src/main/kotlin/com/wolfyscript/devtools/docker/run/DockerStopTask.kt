package com.wolfyscript.devtools.docker.run

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class DockerStopTask @Inject constructor(private var execOperations: ExecOperations): DefaultTask() {

    @get:Input
    val name: Property<String> = project.objects.property<String>()

    @get:Input
    val ignoreExitValue: Property<Boolean> = project.objects.property<Boolean>().convention(true)

    fun applyExtension(extension: DockerRunExtension) {
        name.convention(extension.name)
        ignoreExitValue.convention(extension.ignoreExitValue)
    }

    @TaskAction
    fun stopDockerContainer() {
        val args = listOf("docker", "stop", name.get())

        execOperations.exec {
            isIgnoreExitValue = ignoreExitValue.getOrElse(true)
            commandLine(args)
        }
    }

}