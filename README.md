## Dev-Tools
### Repository
The plugins are published to a private repository that you need to add to the plugin repositories.
> **settings.gradle.kts** 
> ```kotlin
> pluginManagement {
>     repositories {
>         maven("https://artifacts.wolfyscript.com/artifactory/gradle-dev")
>         // ... (other repos)
>     }
> }
> ```

### Docker Run Gradle Plugin
Inspired by docker-run module in [palantir/gradle-docker](https://github.com/palantir/gradle-docker), 
but with a slightly different behaviour.  
It allows you to create custom independent tasks with different configurations instead of just having a single configuration available.  
One extension that can be used to specify default values for all custom tasks.

Add the plugin dependency to the `build.gradle.kts`
```kotlin
plugins {
    id("com.wolfyscript.devtools.docker.run") version ("a2.0.0.2")
}
```

Use the extension to configure the container to run, or specify defaults for custom tasks
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

#### Creating custom tasks 
Simply create a new task of the given type.  
Then apply the defaults if necessary, or not to create an independent task.
```kotlin
target.task<DockerStopTask>("container_stop") {
    applyExtension(extensions.getByName<DockerRunExtension>("dockerRun")) // Apply defaults
    name.set("${name.get()}_$serverName") // overrides default
}

target.task<DockerRunTask>("container_run") {
    applyExtension(extensions.getByName<DockerRunExtension>("dockerRun")) // Apply defaults
    name.set("${name.get()}_$serverName") // overrides default
}
```

#### Use custom extensions
```kotlin
val defaultCustomExt = target.extensions.create<DockerRunExtension>("customDockerRun").apply {
    // Configuration 
}

target.task<DockerStopTask>("container_stop") {
    applyExtension(defaultCustomExt) // Apply defaults
}

target.task<DockerRunTask>("container_run") {
    applyExtension(defaultCustomExt)
}

```

### Minecraft Servers Gradle Plugin
This plugin uses the docker run plugin to run minecraft servers via gradle tasks inside of docker containers.
It uses the [itzg/minecraft-server](https://github.com/itzg/docker-minecraft-server) image, and can be freely configured for different server types.

Add the plugin dependency to the `build.gradle.kts`
```kotlin
plugins {
    id("com.wolfyscript.devtools.docker.minecraft_servers") version ("2.0-SNAPSHOT")
}
```
The plugin can then be configured using the `minecraftServers` extension.
Some properties like the `serversDir`, `libName`, `libDir` are used as defaults for the specified servers in `servers`.

Each server entry in `servers` needs a unique name, that is used for the directory, container, and task.  
A custom directory can be specified via `serverDir`, that will be used instead of the default one.  
The same applies to `libName` and `libDir`, which also override the defaults.  
`version` = the minecraft version of the server  (See [image docs](https://docker-minecraft-server.readthedocs.io/en/latest/versions/minecraft/))  
`type` = the type of the server (See [image docs](https://docker-minecraft-server.readthedocs.io/en/latest/types-and-platforms/))  
`ports` = a list of ports to publish and map from container to host 

```kotlin
minecraftServers {
    serversDir.set(file("./test_servers"))
    libName.set("${project.name}-${version}.jar")
    servers {
        register("spigot_1_17") {
            version.set("1.17.1")
            type.set("SPIGOT")
            ports.set(setOf("25565"))
        }
    }
}
```