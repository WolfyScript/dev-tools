package com.wolfyscript.devtools.docker.run

enum class ContainerPlatform(val command: String) {

    DOCKER("docker"),
    PODMAN("podman")

}