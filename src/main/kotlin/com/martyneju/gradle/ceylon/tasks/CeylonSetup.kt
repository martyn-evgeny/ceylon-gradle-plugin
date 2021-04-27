package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.utils.ceylonDir
import com.martyneju.gradle.ceylon.utils.isWindows
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.util.zip.ZipFile

open class CeylonSetup: DefaultTask() {

    init {
        group = "ceylon"
        description = "Setup ceylon 1.3.3"
        onlyIf {
            !project.ceylonDir.exists()
        }
    }

    @TaskAction
    fun setup() = with(project) {
        ceylonDir.mkdirs()
        val ceylonZip = ceylonDir.resolve("ceylon-1.3.3.zip")
        val ceylonUnzip = ceylonDir.resolve("ceylon-1.3.3")

        val ceylonInputStream = URL("https://ceylon-lang.org/download/dist/1_3_3").openStream()
        ceylonInputStream.use { inputStream ->
            ceylonZip.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        unzip(ceylonZip,ceylonDir)

        ceylonZip.delete()

        if (!isWindows)
            exec {
                it.executable = "chmod"
                it.args("-R","777", ceylonUnzip.absolutePath)
            }
    }

    private fun unzip(name: File, dir: File) {
        ZipFile(name).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.isDirectory)
                    dir.resolve(entry.name).mkdir()
                else
                    zip.getInputStream(entry).use { input ->
                        dir.resolve(entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
            }
        }
    }

}

