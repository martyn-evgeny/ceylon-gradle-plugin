package com.martyneju.gradle.ceylon.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.process.ExecResult
import java.io.*

open class CeylonRunner {
    companion object {
        val log = Logging.getLogger( CeylonRunner::class.java )

        fun run(
            ceylonDirective: String,
            module: String,
            project: Project,
            options: List<CommandOption>,
            additionalArgs: List<String>,
            standardInput: InputStream,
            standardOutput: OutputStream
        ): ExecResult {

            log.info("Executing ceylon ${ceylonDirective} in poject ${project.name}")
            val ceylonArgs = if (project.hasProperty("ceylon-arg"))
                project.property("ceylon-arg")!!.toString()
            else ""

            val ceylon = CeylonToolLocator.findCeylon(project)
            log.debug("Running Ceylon executable: ${ceylon}")

            val env = mapOf<String, String>(
                "JAVA_HOME" to project.file(project.ceylonPlugin.javaLocation.get()).absolutePath,
                "JRE_HOME" to project.file(project.ceylonPlugin.javaLocation.get()).resolve("jre").absolutePath,
                "PATH" to project.file(project.ceylonPlugin.javaLocation.get()).resolve("bin").absolutePath
            )
            val command = (listOf(ceylon, ceylonDirective) +
                    options.map { it.toString() } +
                    ceylonArgs +
                    module +
                    additionalArgs).filter { it.isNotEmpty() }

            env.forEach { log.info(it.key + "=" + it.value) }
            log.info("Running command: ${command.joinToString(" ")}")

            return project.exec {
                it.commandLine(command)
                it.workingDir(project.projectDir)
                it.environment(env)
                it.standardInput = standardInput
                it.standardOutput = standardOutput
            }

        }
    }
}