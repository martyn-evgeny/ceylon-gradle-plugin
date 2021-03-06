package com.martyneju.gradle.ceylon.utils

import org.gradle.api.Project
import java.io.File

open class CeylonCommandOptions {
    companion object {
//        val log = Logging.getLogger(CeylonCommandOptions::class.java)

        fun outDir(project:Project) = project.file(project.ceylonPlugin.output.get()).absolutePath

        fun getCommonOptions(project: Project, includeFlatClasspath: Boolean = true): List<CommandOption> {
            val options = mutableListOf<CommandOption>()
            if (project.file(project.ceylonPlugin.overrides.get()).exists()) options.add(
                CommandOption(
                    "--overrides",
                    project.file(project.ceylonPlugin.overrides.get()).path
                )
            )
            if (includeFlatClasspath && project.ceylonPlugin.flatClasspath.get()) options.add(CommandOption("--flat-classpath"))
            options.addAll(getRepositoryOptions(project))
            return options
        }

        fun getRepositoryOptions(project: Project) = listOf(
//            CommandOption("--rep", "aether${project.file(project.ceylonPlugin.mavenSettings.get()).path}"),
            CommandOption("--rep", project.ceylonPlugin.output.get())
        )

        fun getFatJarOptions(project: Project, entryPoint: String): List<CommandOption> {
            val options = mutableListOf<CommandOption>()
            options.add(CommandOption("--out", outDir(project)))
            options.add(CommandOption("--run", entryPoint))
            options.addAll(getCommonOptions(project))
            return options
        }

        fun getRunOptions(project: Project, entryPoin: String): List<CommandOption> {
            val options = mutableListOf<CommandOption>()
            options.add(CommandOption("--run",entryPoin))
            options.addAll(getCommonOptions(project))
            return options
        }

        fun getTestOptions(project: Project): List<CommandOption> {
            val options = mutableListOf<CommandOption>()
            if(project.ceylonPlugin.generateTestReport.get()) options.add(CommandOption("--report"))
            options.addAll(getCommonOptions(project))
            return options
        }

        fun getImportJarsOptions(project: Project, moduleDescriptor: File): List<CommandOption> {
            val options = mutableListOf<CommandOption>()
            if(project.ceylonPlugin.verbose.get()) options.add(CommandOption("--verbose"))
//            if(project.ceylonPlugin.forceImports.get()) options.add(CommandOption("--force"))
            options.add(CommandOption("--descriptor", moduleDescriptor.absolutePath))
            options.add(CommandOption("--out", outDir(project)))
            options.add(CommandOption(( "--show-suggestions")))
            options.addAll(getRepositoryOptions(project))
            return options
        }

        fun getCompileOptions(project: Project): List<CommandOption> {
            val options = mutableListOf<CommandOption>()
            options.add(CommandOption("--out", outDir(project)))
            project.ceylonPlugin.sourceRoots.get().forEach {
                options.add(CommandOption("--source", it))
            }
            project.ceylonPlugin.resourceRoots.get().forEach {
                options.add(CommandOption("--resource", it))
            }
            options.addAll(getCommonOptions(project))
            return options
        }
    }
}