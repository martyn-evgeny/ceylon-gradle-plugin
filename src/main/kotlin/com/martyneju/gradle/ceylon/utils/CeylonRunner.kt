package com.martyneju.gradle.ceylon.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.process.ExecResult
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream

open class CeylonRunner {
    companion object {
        val log = Logging.getLogger( CeylonRunner::class.java )

        fun run(
            ceylonDirective: String,
            module: String,
            project: Project,
            options: List<CommandOption>,
            standardInput: InputStream,
            standardOutput: OutputStream
        ): ExecResult {

            log.info("Executing ceylon ${ceylonDirective} in poject ${project.name}")
            val ceylonArgs = project.property("ceylon-arg")?.toString() ?: ""

            val ceylon = CeylonToolLocator.findCeylon(project)
            log.debug("Running Ceylon executable: ${ceylon}")


            val env = mapOf<String,String> (
                "JAVA_HOME" to project.ceylonPlugin.javaLocation.get().absolutePath,
                "JRE_HOME" to project.ceylonPlugin.javaLocation.get().resolve("jre").absolutePath,
                "PATH" to project.ceylonPlugin.javaLocation.get().resolve("bin").absolutePath
            )
            val command = listOf(ceylon, ceylonDirective) +
                options.map { it.toString() } +
                ceylonArgs +
                module

            env.forEach { log.info(it.key+"="+it.value) }
            log.info( "Running command: ${command.joinToString { " " }}" )

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