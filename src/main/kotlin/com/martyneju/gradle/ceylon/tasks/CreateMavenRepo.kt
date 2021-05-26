package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.MavenSettingsFileCreator
import com.martyneju.gradle.ceylon.utils.dependency.ResolveCeylonDependencies
import com.martyneju.gradle.ceylon.utils.linear
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.*
import java.io.File

@Deprecated("need change")
open class CreateMavenRepo: DefaultTask() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """
        |Creates a local Maven repository containing all transitive dependencies.
        |The repository uses the default Maven repository format and is used by all Ceylon commands
        |so that Ceylon is not required to search for any Maven dependency in a remote repository,
        |ensuring that Gradle is used as the main Maven dependency resolver.
        """.linear()
    }

    val log = Logging.getLogger(CreateMavenRepo::class.java)

    /**
     *   name of the Ceylon module
     *   mandatory if no Ceylon config file exists
     *   and no add properties when exec task
     */
    @Input
    var ceylonModule: String =
        if (project.hasProperty("ceylonModule")) project.property("ceylonModule").toString() else ""

    private val dependencies by lazy { ResolveCeylonDependencies(project, ceylonModule) }

    @InputFiles
    fun inputFiles() =
        project.allprojects.map { it.buildFile } + dependencies.moduleFile

    private val rootDir = project.buildDir.resolve("maven-repository")

    @OutputDirectory
    fun outputDirectory() = rootDir

    @OutputFiles
    fun outputFiles() = listOf(MavenSettingsFileCreator.mavenSettingsFile(project))

    @TaskAction
    fun run() {
        MavenSettingsFileCreator.createMavenSettingsFile(project)
        dependencies.resolve().jarDependencies.forEach {
            val dir = dirDependency(it, rootDir)
            copyDependency(it, dir)
            copyPom(it, dir)
        }
    }

    private fun dirDependency(dependency: ResolvedDependency, rootDir: File): File =
        dependency
            .moduleGroup
            .split(".")
            .fold(rootDir) { acc, it -> acc.resolve(it) }
            .resolve(dependency.moduleName)
            .resolve(dependency.moduleVersion)

    private fun copyDependency(dependency: ResolvedDependency, dirDependency: File ) {
        project.copy{
            it.from(dependency.moduleArtifacts.map { ar -> ar.file })
            it.into(dirDependency)
        }
    }

    private fun copyPom(dependency: ResolvedDependency, dirDependency: File) {
        project.copy {
            it.from(CreateDependenciesPoms.pomTempLocation(dependency, project))
            it.into(dirDependency)
        }
    }
}