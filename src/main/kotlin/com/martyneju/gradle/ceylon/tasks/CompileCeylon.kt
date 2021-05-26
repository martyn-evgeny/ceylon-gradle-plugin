package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.CeylonCommandOptions
import com.martyneju.gradle.ceylon.utils.CeylonRunner
import com.martyneju.gradle.ceylon.utils.ceylonPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.streams.SafeStreams
import java.io.InputStream
import java.io.OutputStream

open class CompileCeylon: DefaultTask() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = "Compiles Ceylon and Java source code"
    }

    val log = Logging.getLogger(GenerateOverridesFile::class.java)

    /**
     *   name of the Ceylon module which will compile
     *   you can add properties when exec task
     *   if not set this parametr Ceylon will compile all source
     */
    @Input
    var ceylonModule: String =
        if (project.hasProperty("ceylonModule")) project.property("ceylonModule").toString() else ""

    @Input
    var standardInput: InputStream = SafeStreams.emptyInput()

    @Input
    var standardOutput: OutputStream = SafeStreams.systemOut()

    @InputFiles
    fun inputFiles() =  listOf(
        project.buildFile,
        project.file(project.ceylonPlugin.sourceRoots.get()),
        project.file(project.ceylonPlugin.resourceRoots.get())
    )

    @OutputDirectory
    fun outputDirectory() = project.file(project.ceylonPlugin.output.get())

    @TaskAction
    fun run() {
        CeylonRunner.run("compile",
            ceylonModule,
            project,
            CeylonCommandOptions.getCompileOptions(project),
            listOf(),
            standardInput,
            standardOutput
        )
    }
}