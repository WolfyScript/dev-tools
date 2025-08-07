## Dev-Tools
### Repository
The plugins are published to a private repository that you need to add to the plugin repositories.  

**settings.gradle.kts** 
```kotlin
pluginManagement {
    repositories {
        maven("https://artifacts.wolfyscript.com/artifactory/gradle-dev")
        // ... (other repos)
    }
}
```

# Docker Run Gradle Plugin
This plugin simplifies running docker containers using Gradle and compose custom setups.

Add the plugin dependency to the `build.gradle.kts`
```kotlin
plugins {
    id("com.wolfyscript.devtools.docker.run") version ("a2.1.0.1")
}
```
> [!note]
> 
> The plugin has only been tested on **Linux** using a local **Docker Engine** install.
> The plugin runs all the docker commands on the command line provided by Gradle.
> It is unknown how well it functions on Windows and/or Docker Desktop.
> 


## Default Behaviour
The plugin provides an extension `dockerRun` and 3 tasks (`dockerRun`, `dockerStatus`, and `dockerStop`). 

Use the extension to configure the container to run or specify defaults for custom tasks
```kotlin
dockerRun {
    name.set("test_minecraft_server")
    image.set("itzg/minecraft-server")
    daemonize.set(true)
    clean.set(true) // create a temporary server container
    ports("25565:25565")
    env(buildMap {
        this["TYPE"] = "SPIGOT"
        this["VERSION"] = "1.20.1"
    })
    arguments.set(setOf("-it")) // Allow for console interactivity with 'docker attach'
}
```
The tasks can then be used to either run, stop, or get the status of the associated container.

## Custom Behaviour
This plugin allows you to create custom independent tasks and configure them independently of each other.  
You can also create custom extensions that can be used to specify default values for your custom tasks.  
This makes it possible to run multiple different containers.  

### Creating custom tasks 
Create custom run, stop, or status tasks.    
The `applyExtension` allows to easily apply defaults that are specified in the given `DockerRunExtension`. 
If not used, the task is completely independent.
```kotlin
target.task<DockerStopTask>("container_stop") {
    applyExtension(extensions.getByName<DockerRunExtension>("dockerRun")) // Apply defaults as conventions
    name.set("${name.get()}_$serverName") // overrides default
}

target.task<DockerRunTask>("container_run") {
    applyExtension(extensions.getByName<DockerRunExtension>("dockerRun")) // Apply defaults as conventions
    name.set("${name.get()}_$serverName") // overrides default
}
```

### Use custom extensions
```kotlin
val defaultCustomExt = target.extensions.create<DockerRunExtension>("customDockerRun").apply {
    // Configuration 
}

target.task<DockerStopTask>("container_stop") {
    applyExtension(defaultCustomExt) // Apply defaults as conventions
}

target.task<DockerRunTask>("container_run") {
    applyExtension(defaultCustomExt)
}

```

# Minecraft Servers Gradle Plugin
This plugin uses the docker run plugin to simplify running minecraft servers via Gradle tasks inside docker containers.
It uses the [itzg/minecraft-server](https://github.com/itzg/docker-minecraft-server) image, and can be freely configured for different server types.

Add the plugin dependency to the `build.gradle.kts`
```kotlin
plugins {
    id("com.wolfyscript.devtools.docker.minecraft_servers") version ("a2.1.0.1")
}
```
## Configuring Servers
The servers can then be configured using the `minecraftServers` extension.   
Some properties like the `serversDir`, `libName`, `libDir` are used as defaults for the specified servers in `servers`.

All server data is stored within the specified host `serversDir` directory. The default directory is the project directory. 

Each server entry in `servers` needs to be registered under a unique name, and is then stored within a subdirectory of the same name.
The property defaults from the `minecraftServers` extension may be overridden by each server seperately. 

`version`: the minecraft version of the server  (See [image docs](https://docker-minecraft-server.readthedocs.io/en/latest/versions/minecraft/))  
`type`: the type of the server (See [image docs](https://docker-minecraft-server.readthedocs.io/en/latest/types-and-platforms/))  
`ports`: a list of ports to publish and map from container to host 

```kotlin
minecraftServers {
    serversDir.set(file("./test_servers"))
    libName.set("${project.name}-${version}.jar")
    servers {
        register("spigot") {
            version.set("1.21.6")
            type.set("SPIGOT")
            ports.add("25565") 
            destFileName.set("scafall.jar") // set a name for the copied app
            imageVersion.set("java21") // Use a different version of the docker image
        }
    }
}
```

## Configuring Minecraft Docker
The plugin also provides the `minecraftDockerRun` that allows configuring the docker containers itself.

Here is an example from scafall:
The main purpose here is to have the duplicated code in a buildSrc plugin. Including a debugger connection, resource limits, and other defaults.
```kotlin
val debugPort: String = System.getenv("debugPort") ?: "5006"
val debugPortMapping = "${debugPort}:${debugPort}"

minecraftDockerRun {
    // By default, the container is removed when stopped. 
    // That makes it impossible to know why a container may fail to start.
    // In that case disable it to debug and delete the container manually.
    // clean.set(false)
    env.putAll(
        mapOf(
            // Limit each container memory
            "MEMORY" to "2G",
            // Allows attaching the IntelliJ Debugger
            "JVM_OPTS" to "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${debugPort}",
            "FORCE_REDOWNLOAD" to "false"
        )
    )
    arguments(
        // Constrain to only use 2 cpus to better align with real production servers 
        "--cpus",
        "2",
        // allow console interactivity (docker attach)
        "-it"
    )
    ports.set(listOf(debugPortMapping))
}
```


