import com.martyneju.gradle.ceylon.Config
import com.lazan.dependency.export.MavenDependencyExport

plugins {
    java
    id("maven-publish")
    id("com.lazan.dependency-export") version "0.5"
}

group = "com.martyneju.gradle.ceylon.example.sample"
version = "0.0.1"

apply(plugin = "com.martyneju.gradle.ceylon")

repositories {
    mavenLocal()
    mavenCentral()
}

val ceylonRuntime = project.configurations.findByName("ceylonRuntime")!!

dependencies {
    //implementation("org.ceylon-lang:com.redhat.ceylon.common:1.3.3")
    //testCompile("junit", "junit", "4.12")
    implementation("org.apache.logging.log4j:log4j-core:2.4.1")
}

configurations.implementation.isCanBeResolved = true
var config = project.extensions.getByType(Config::class.java)


tasks.register<MavenDependencyExport>("dep") {
    configuration("implementation")
    exportSources = true
    exportJavadoc = true
    exportDir = project.file("dep").resolve("m2").resolve("repository")
}