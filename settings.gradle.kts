pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "dev-tools"

include("docker-tools")
include("buildtools")

project(":docker-tools").projectDir = file("docker-tools")
project(":buildtools").projectDir = file("buildtools")
