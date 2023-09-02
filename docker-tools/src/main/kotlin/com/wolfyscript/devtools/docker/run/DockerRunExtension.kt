package com.wolfyscript.devtools.docker.run

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class DockerRunExtension {

    /**
     * The name of the container
     */
    @get:Input
    abstract val name: Property<String>

    /**
     * The image of the container
     */
    @get:Input
    abstract val image: Property<String>

    /**
     * The network to add the container to
     */
    @get:Input
    @get:Optional
    abstract val network: Property<String>

    /**
     * Specifies if the container should be run as a daemon
     * Enabled by default, if disabled will run the container inside the task and stopping the task will stop the container.
     */
    @get:Input
    @get:Optional
    abstract val daemonize: Property<Boolean>

    /**
     * Specifies if the container should be removed when stopped.
     * Enabled by default.
     * When disabled it may cause issues when starting another container with the same name!
     */
    @get:Input
    @get:Optional
    abstract val clean: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val ignoreExitValue: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val command: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val env: MapProperty<String, String>

    abstract val ports: SetProperty<String>

    abstract val arguments: ListProperty<String>

    /**
     * Volume mappings and bindings of the container
     */
    abstract val volumes: MapProperty<Any, String>

    fun arguments(vararg arguments: String) {
        this.arguments.set(listOf(elements = arguments))
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
        this.ports.set(buildSet {
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
        this.volumes.set(volumes.toMap())
    }

    fun env(env: Map<String, String>) {
        this.env.set(env.toMap())
    }

}