plugins {
    java
}

buildscript {
    repositories {
        mavenLocal()
        jcenter()
    }

    dependencies {
        classpath( "com.martyneju.gradle.ceylon:ceylon-gradle-plugin:0.0.1")
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