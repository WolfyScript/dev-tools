package com.wolfyscript.devtools.docker.minecraft

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Optional

interface ServerEntry {

    val name : String

    val type : Property<String>

    val version : Property<String>

    val ports: SetProperty<String>

    @get:Optional
    val serverDir: DirectoryProperty

    @get:Optional
    val libName: Property<String>

    @get:Optional
    val libDir: DirectoryProperty
}