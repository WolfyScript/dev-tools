plugins {
    `java-library`
    `maven-publish`
    id("com.jfrog.artifactory")
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "com.wolfyscript.devtools"
version = "2.0-SNAPSHOT"
