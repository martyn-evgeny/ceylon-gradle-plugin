package com.martyneju.gradle.ceylon.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.InputStream
import java.io.PrintStream

open class CeylonRunner {
    companion object {
        val log = Logging.getLogger( CeylonRunner::class.java )

        fun withCeylon(project: Project, ceylonConsumer: (String)->Unit) {
            val ceylon = CeylonToolLocator.findCeylon(project)
            log.debug("Running Ceylon executable: ${ceylon}")
            try {
                ceylonConsumer(ceylon)
            } catch(e: GradleException) {
                throw e
            } catch (e: Throwable) {
                throw  GradleException("Problem running the ceylon command. Run with --stacktrace for the cause.",e)
            }
        }

        fun run(ceylonDirective: String, module: String, project: Project, options: List<CommandOption>, args: List<String>) {

            log.info("Executing ceylon ${ceylonDirective} in poject ${project.name}")
            val ceylonArgs = project.property("ceylon-arg")?.toString() ?: ""

            withCeylon(project) { ceylon ->
                val env = mapOf<String,String> (
                    "JAVA_HOME" to project.ceylonPlugin.javaLocation.get().absolutePath,
                    "JRE_HOME" to project.ceylonPlugin.javaLocation.get().resolve("jre").absolutePath,
                    "PATH" to project.ceylonPlugin.javaLocation.get().resolve("bin").absolutePath
                )
                val command = listOf(ceylon, ceylonDirective) +
                    options.map { it.toString() } +
                    ceylonArgs +
                    module +
                    args

                if (project.hasProperty("get-ceylon-command")) {
                    env.forEach { println(it.key+"="+it.value) }
                    println(command.joinToString { " " })
                } else {
                    env.forEach { log.info(it.key+"="+it.value) }
                    log.info( "Running command: ${command.joinToString { " " }}" )

                    val builder = ProcessBuilder(command)
                    builder.inheritIO();
                    builder.environment().putAll(env)
                    var exitCode = -1
                    try {
                        exitCode = builder.start().waitFor()
                        log.debug("Ceylon process finished with code $exitCode")
                    } catch (e: Throwable) {
                        log.error("A problem has occurred while trying to run the Ceylon command",e)
                    }

                    if ( exitCode != 0 ) {
                        throw GradleException( "Ceylon process exited with code $exitCode. " +
                                "See output for details." )
                    }
                }
            }
        }

    }
}