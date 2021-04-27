import com.martyneju.gradle.ceylon.Config

plugins {
    java
}

buildscript {
    repositories {
        mavenLocal()
        jcenter()
    }

    dependencies {
        classpath( "com.martyneju:ceylon-plugin:0.0.1")
    }
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
