import com.martyneju.gradle.ceylon.Config
import com.martyneju.gradle.ceylon.tasks.GenerateOverridesFile

plugins {
    java
}

group = "com.martyneju.gradle.ceylon.example.sample"
version = "0.0.1"

repositories {
    mavenCentral()
}

apply(plugin = "com.martyneju.gradle.ceylon")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.ceylon-lang:com.redhat.ceylon.common:1.3.3")
    testCompile("junit", "junit", "4.12")
}

var config = project.extensions.getByType(Config::class.java)

tasks.register<GenerateOverridesFile>("override") {
    ceylonModule = "com.martyneju.gradle.ceylon.example.sample"
}