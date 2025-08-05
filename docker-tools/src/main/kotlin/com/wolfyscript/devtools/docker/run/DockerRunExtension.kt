package com.wolfyscript.devtools.docker.run

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

abstract class DockerRunExtension(objects: ObjectFactory, project: Project) {

    /**
     * The name of the container
     */
    @get:Input
    val name: Property<String> = objects.property<String>()

    /**
     * The image of the container
     */
    @get:Input
    val image: Property<String> = objects.property<String>()

    /**
     * The network to add the container to
     */
    @get:Input
    @get:Optional
    val network: Property<String> = objects.property<String>()

    /**
     * Specifies if the container should be run as a daemon
     * Enabled by default, if disabled will run the container inside the task and stopping the task will stop the container.
     */
    @get:Input
    @get:Optional
    val daemonize: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * Specifies if the container should be removed when stopped.
     * Enabled by default.
     * When disabled it may cause issues when starting another container with the same name!
     */
    @get:Input
    @get:Optional
    val clean: Property<Boolean> = objects.property<Boolean>().convention(true)

    @get:Input
    @get:Optional
    val ignoreExitValue: Property<Boolean> = objects.property<Boolean>().convention(true)

    @get:Input
    @get:Optional
    val command: ListProperty<String> = objects.listProperty<String>()

    @get:Input
    @get:Optional
    val env: MapProperty<String, String> = objects.mapProperty<String, String>()

    val ports: SetProperty<String> = objects.setProperty<String>()

    val arguments: ListProperty<String> = objects.listProperty<String>()

    /**
     * Volume mappings and bindings of the container
     */
    val volumes: MapProperty<Any, String> = objects.mapProperty<Any, String>().empty()

    fun arguments(vararg arguments: String) {
        this.arguments.addAll(*arguments)
    }

    /**
     * Specifies the port mappings of the container.
     *
     * Either '&lt;port&gt;&#39; or &#39;&lt;host_port&gt;:&lt;container_port&gt;'.
     *
     * Each port must be in the range of [1, 65536]
     *
     * @param ports List of port mappings
     */
    fun ports(vararg ports: String) {
        this.ports.addAll(buildSet {
            for (port in ports) {
                val mapping = port.split(":", limit = 2)
                if (mapping.size == 1) {
                    isPortValid(mapping[0])
                    this.add("${mapping[0]}:${mapping[0]}")
                } else {
                    isPortValid(mapping[0])
                    isPortValid(mapping[1])
                    this.add("${mapping[0]}:${mapping[1]}")
                }
            }
        })
    }

    private fun isPortValid(port: String) {
        if (Integer.parseInt(port) !in 1..65536) {
            throw IllegalArgumentException("Port must be in the range [1,65536]")
        }
    }

    fun volumes(volumes: Map<Any, String>) {
        this.volumes.putAll(volumes)
    }

    fun env(env: Map<String, String>) {
        this.env.putAll(env)
    }

}