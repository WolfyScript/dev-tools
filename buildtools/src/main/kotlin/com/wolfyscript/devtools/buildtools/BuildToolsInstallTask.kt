package com.wolfyscript.devtools.buildtools

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher

abstract class BuildToolsInstallTask : JavaExec() {

    @get:InputFile
    @get:Optional
    val buildToolsJar: RegularFileProperty = this.project.objects.fileProperty().convention(this.project.layout.projectDirectory.dir("buildtools").file("BuildTools.jar"))

    @get:InputDirectory
    @get:Optional
    val buildToolsDir: DirectoryProperty = this.project.objects.directoryProperty().convention(this.project.layout.projectDirectory.dir("buildtools"))

    @get:Input
    abstract val minecraftVersion : Property<String>

    override fun exec() {
        val dir = buildToolsDir.get()
        // Run build tools for specified version
        // This installs the minecraft version into the maven repo to use in the classpath
        classpath(buildToolsJar)
        workingDir(dir)
            args(
                "--rev",
                minecraftVersion.orElse("1.20.1").get(),
                "--remapped"
            )
            super.exec()

    }

}