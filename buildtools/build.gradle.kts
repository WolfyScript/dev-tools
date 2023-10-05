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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        register("buildtools") {
            id = "com.wolfyscript.devtools.buildtools"
            implementationClass = "com.wolfyscript.devtools.buildtools.BuildToolsPlugin"
        }
    }
}
