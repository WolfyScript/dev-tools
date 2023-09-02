plugins {
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.wolfyscript.devtools.java-conventions")
}

description = "docker-tools"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        register("docker-run") {
            id = "com.wolfyscript.devtools.docker.run"
            implementationClass = "com.wolfyscript.devtools.docker.run.DockerRunPlugin"
        }
        register("minecraft-servers") {
            id = "com.wolfyscript.devtools.docker.minecraft_servers"
            implementationClass = "com.wolfyscript.devtools.docker.minecraft.MinecraftServersPlugin"
        }
    }
}
