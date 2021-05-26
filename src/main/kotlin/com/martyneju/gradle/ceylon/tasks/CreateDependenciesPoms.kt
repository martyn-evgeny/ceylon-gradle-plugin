package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.MavenPomCreator
import com.martyneju.gradle.ceylon.utils.dependency.DependencyTree
import com.martyneju.gradle.ceylon.utils.dependency.ResolveCeylonDependencies
import com.martyneju.gradle.ceylon.utils.linear
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@Deprecated("need change")
open class CreateDependenciesPoms: DefaultTask() {

    companion object {
        fun getRootDir(project: Project) = project.buildDir.resolve("dependency-poms")
        fun pomTempLocation(dependency: ResolvedDependency, project: Project) =
            getRootDir(project).resolve("${dependency.moduleName}-${dependency.moduleVersion}.pom")
    }

    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """
            |Creates Maven pom files for all transitive dependencies.
            |The transitive dependencies are resolved by Gradle, then for each dependency, a pom
            |is created containing only its direct dependencies as reported by Gradle.
            |This allows Ceylon to import jars without considering optional Maven dependencies,
            |for example, as Gradle does not resolve optional dependencies by default.
            """.linear()
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

    private val rootDir = getRootDir(project)

    @OutputDirectory
    fun outputDirectory() = rootDir

    @TaskAction
    fun run() {
        if(!rootDir.exists()) rootDir.mkdirs()
        dependencies.resolve().jarDependencies.forEach {
            MavenPomCreator.createPomFor(
                it,
                DependencyTree.directDependenciesOf(it),
                pomTempLocation(it,project)
            )
        }
    }
}