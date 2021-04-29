package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.utils.arch
import com.martyneju.gradle.ceylon.utils.ceylonDir
import com.martyneju.gradle.ceylon.utils.isWindows
import com.martyneju.gradle.ceylon.utils.os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.util.zip.ZipFile
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import java.io.BufferedInputStream
import java.nio.file.Files

open class CeylonSetup: DefaultTask() {

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
        val ceylonUnzip = ceylonDir.resolve("ceylon-1.3.3")
        download("https://ceylon-lang.org/download/dist/1_3_3", ceylonZip)
        unzip(ceylonZip,ceylonDir)
        ceylonZip.delete()

        val suf = if(isWindows) ".zip" else ".tar.gz"
        val javaZip = ceylonDir.resolve("jdk8u292-b10${suf}")
        val javaUnzip = ceylonDir.resolve("jdk8u292-b10")
        download("https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u292-b10/OpenJDK8U-jdk_${arch}_${os}_hotspot_8u292b10${suf}", javaZip)
        if(isWindows) unzip(javaZip,ceylonDir) else untargz(javaZip, ceylonDir)
        javaZip.delete()

        if (!isWindows)
            exec {
                it.executable = "chmod"
                it.args("-R","777", ceylonUnzip.absolutePath)
            }
            exec {
                it.executable = "chmod"
                it.args("-R","777", javaUnzip.absolutePath)
            }
    }

    private fun download(url: String, resFile: File) {
        val openStream = URL(url).openStream()
        openStream.use { inputStream ->
            resFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun untargz(name: File, dir: File) {
        name.inputStream().use { input ->
            TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(input))).use { tar ->
                var entry: ArchiveEntry? = tar.nextEntry
                while (entry != null) {
                    if ( entry.isDirectory ) dir.resolve(entry.name).mkdir()
                    else Files.copy(tar, dir.resolve(entry.name).toPath())
                    entry = tar.nextEntry
                }
            }
        }
    }

    private fun unzip(name: File, dir: File) {

        ZipFile(name).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.isDirectory) dir.resolve(entry.name).mkdir()
                else Files.copy(zip.getInputStream(entry), dir.resolve(entry.name).toPath())
            }
        }
    }

}

