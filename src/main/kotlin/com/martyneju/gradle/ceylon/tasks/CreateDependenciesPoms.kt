package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.MavenPomCreator
import com.martyneju.gradle.ceylon.utils.dependency.DependencyTree
import com.martyneju.gradle.ceylon.utils.dependency.ResolveCeylonDependencies
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

open class CreateDependenciesPoms: DefaultTask() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """
            | Creates Maven pom files for all transitive dependencies.
            | The transitive dependencies are resolved by Gradle, then for each dependency, a pom
            | is created containing only its direct dependencies as reported by Gradle.
            | This allows Ceylon to import jars without considering optional Maven dependencies,
            | for example, as Gradle does not resolve optional dependencies by default.
            """.trimMargin()
    }

    val log = Logging.getLogger(GenerateOverridesFile::class.java)

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

    private val rootDir = project.buildDir.resolve("dependency-poms")

    @OutputDirectory
    fun outputFiles() = listOf(rootDir)

    @TaskAction
    fun run() {
        if(!rootDir.exists()) rootDir.mkdirs()
        dependencies.resolve().jarDependencies.forEach {
            MavenPomCreator.createPomFor(
                it,
                DependencyTree.directDependenciesOf(it),
                rootDir.resolve("${it.moduleName}-${it.moduleVersion}.pom")
            )
        }
    }
}