package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.utils.arch
import com.martyneju.gradle.ceylon.utils.ceylonDir
import com.martyneju.gradle.ceylon.utils.isWindows
import com.martyneju.gradle.ceylon.utils.os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class Setup: DefaultTask() {

    init {
        group = "ceylon"
        description = "Setup ceylon 1.3.3 and AdoptOpenJDK (jdk8u292-b10)"
        onlyIf {
            !project.ceylonDir.exists()
        }
    }

    @TaskAction
    fun setup() = with(project) {
        ceylonDir.mkdirs()

        val ceylonZip = ceylonDir.resolve("ceylon-1.3.3.zip")
        ant.invokeMethod("get",mapOf(
            "src" to "https://ceylon-lang.org/download/dist/1_3_3",
            "dest" to ceylonZip))
        ant.invokeMethod("unzip", mapOf(
            "src" to ceylonZip,
            "dest" to ceylonDir))
        ceylonZip.delete()

        val suf = if(isWindows) ".zip" else ".tar.gz"
        val javaZip = ceylonDir.resolve("jdk8u292-b10${suf}")
        ant.invokeMethod("get",mapOf(
            "src" to "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u292-b10/OpenJDK8U-jdk_${arch}_${os}_hotspot_8u292b10${suf}",
            "dest" to javaZip))

        if( isWindows )  ant.invokeMethod("unzip", mapOf(
            "src" to javaZip,
            "dest" to ceylonDir))
        else ant.invokeMethod("untar", mapOf(
            "src" to javaZip,
            "dest" to ceylonDir,
            "compression" to "gzip"
        ))
        javaZip.delete()

        if (!isWindows)
            exec {
                it.executable = "chmod"
                it.args("-R","777", ceylonDir.resolve("ceylon-1.3.3").absolutePath)
            }
            exec {
                it.executable = "chmod"
                it.args("-R","777", ceylonDir.resolve("jdk8u292-b10").absolutePath)
            }
    }

}

