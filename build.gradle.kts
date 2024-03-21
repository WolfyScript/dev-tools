plugins {
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-2"
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    id("com.wolfyscript.devtools.java-conventions")
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

nexusPublishing {
    repositories {
        register("wolfyRepo") {
            nexusUrl.set(uri("https://maven.wolfyscript.com/repository/releases/"))
            snapshotRepositoryUrl.set(uri("https://maven.wolfyscript.com/repository/snapshots/"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            from(components.getByName("java"))
        }
    }
}
