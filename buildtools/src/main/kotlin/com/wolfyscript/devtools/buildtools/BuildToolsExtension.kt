package com.wolfyscript.devtools.buildtools

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

interface BuildToolsExtension {

    /**
     *
     */
    @get:Optional
    val buildToolsDir: DirectoryProperty

    @get:Optional
    val buildToolsJar: RegularFileProperty

    val minecraftVersion : Property<String>
}