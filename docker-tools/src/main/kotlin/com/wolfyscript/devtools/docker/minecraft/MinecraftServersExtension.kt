package com.wolfyscript.devtools.docker.minecraft

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.component.Artifact
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface MinecraftServersExtension {

    val servers : NamedDomainObjectContainer<ServerEntry>

    @get:Optional
    val serversDir : DirectoryProperty

    val libName: Property<String>

    @get:Optional
    val libDir: DirectoryProperty

}