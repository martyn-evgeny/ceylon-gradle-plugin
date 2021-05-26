package com.martyneju.gradle.ceylon.utils

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.File

class MavenSettingsFileCreator {
    companion object {
        val log = Logging.getLogger( MavenSettingsFileCreator::class.java )

        fun mavenSettingsFile(project: Project): File = TODO("need change")
//            project.file(project.ceylonPlugin.mavenSettings.get())

        fun createMavenSettingsFile(project: Project): File {
            TODO("need change")
//            val settingsFile = mavenSettingsFile(project)
//
//            // do not overwrite file if already there
//            if(settingsFile.exists()) {
//                log.info("Maven settings file already exists. Will not overwrite it.")
//                return settingsFile
//            }
//
//            log.info("Creating Maven settings file for Ceylon")
//            settingsFile.parentFile.mkdirs()
//            settingsFile.writeText(""""
//            |<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
//            |    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//            |    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
//            |                        http://maven.apache.org/xsd/settings-1.0.0.xsd">
//            |    <localRepository>${project.file(project.ceylonPlugin.mavenRepo.get()).absolutePath}</localRepository>
//            |    <offline>true</offline>
//            |</settings>
//            |""".trimMargin())
//
//            return settingsFile
        }
    }
}