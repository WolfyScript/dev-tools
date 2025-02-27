import org.jfrog.gradle.plugin.artifactory.Constant.ALL_PUBLICATIONS

plugins {
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    id("com.wolfyscript.devtools.java-conventions")
    id("com.jfrog.artifactory")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    implementation(project(":docker-tools"))
    implementation(project(":buildtools"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

artifactory {
    publish {
        contextUrl = "https://artifacts.wolfyscript.com/artifactory"
        repository {
            repoKey = "gradle-dev-local"
            username = System.getenv("ARTIFACTORY_USERNAME")
            password = System.getenv("ARTIFACTORY_TOKEN")
        }
        defaults {
            publications(ALL_PUBLICATIONS)
            setPublishArtifacts(true)
            setPublishPom(true)
        }
    }
}
