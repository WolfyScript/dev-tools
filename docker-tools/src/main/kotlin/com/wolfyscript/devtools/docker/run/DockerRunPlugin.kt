package com.wolfyscript.devtools.docker.run

import com.wolfyscript.devtools.docker.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class DockerRunPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<DockerRunExtension>(Constants.EXT_DOCKER_RUN_NAME)

        target.tasks.register<DockerRunTask>(Constants.TASK_DOCKER_RUN_NAME) {
            group = Constants.TASK_DOCKER_RUN_GROUP_NAME
            applyExtension(extension)
        }

        target.tasks.register(Constants.TASK_DOCKER_STATUS_NAME) {
            group = Constants.TASK_DOCKER_RUN_GROUP_NAME
        }

        target.tasks.register<DockerStopTask>(Constants.TASK_DOCKER_STOP_NAME) {
            group = Constants.TASK_DOCKER_RUN_GROUP_NAME
            applyExtension(extension)
        }

    }

}
