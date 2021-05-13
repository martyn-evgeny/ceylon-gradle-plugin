import com.martyneju.gradle.ceylon.Config
import com.martyneju.gradle.ceylon.tasks.GenerateOverridesFile

plugins {
    java
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
    ceylonRuntime("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.3")
    ceylonRuntime("org.apache.poi:poi:5.0.0")
}

var config = project.extensions.getByType(Config::class.java)

tasks.register<GenerateOverridesFile>("createOverride") {
    ceylonModule = "com.martyneju.gradle.ceylon.example.sample"
}