plugins {
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
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

publishing {
    publications {
        create<MavenPublication>("lib") {
            from(components.getByName("java"))
        }
    }
    repositories {
        maven {
            name = "wolfyRepo"
            credentials(PasswordCredentials::class)
            url = uri("https://maven.wolfyscript.com/repository/${if (project.version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"}/")
        }
    }
}
