package com.wolfyscript.devtools.docker.minecraft

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Optional

abstract class ServerEntry {

    abstract val name : String

    abstract val type : Property<String>

    @get:Optional
    abstract val imageVersion: Property<String>

    @get:Optional
    abstract val version : Property<String>

    abstract val ports: SetProperty<String>

    @get:Optional
    abstract val serverDir: DirectoryProperty


    @get:Optional
    abstract val libName: Property<String>

    @get:Optional
    abstract val destFileName: Property<String>

    @get:Optional
    abstract val destPath: Property<String>

    @get:Optional
    abstract val libDir: DirectoryProperty

    @get:Optional
    abstract val extraEnv: MapProperty<String, String>

    init {
    }
}