plugins {
    kotlin("jvm") version "1.4.31"
}

group = "com.martyneju"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8


repositories {
    mavenLocal()
    jcenter()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.ceylon-lang:com.redhat.ceylon.common:1.3.3")
}
