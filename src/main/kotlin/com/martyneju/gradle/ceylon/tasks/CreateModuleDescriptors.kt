package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.ModuleDescriptorCreator
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

open class CreateModuleDescriptors: DefaultTask() {
    companion object {
        fun getRootDir(project: Project) = project.buildDir.resolve("module-descriptors")
        fun descriptorTempLocation(dependency: ResolvedDependency, project: Project) =
            getRootDir(project).resolve(
                "${dependency.moduleName}-${dependency.moduleVersion}.properties".replace("-","_")
            )
    }
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """
        |Creates module descriptors (properties files) for all transitive dependencies.
        |The transitive dependencies are resolved by Gradle, then for each dependency, a module
        |so that Ceylon is not required to search for any Maven dependency in a remote repository,
        |descriptor is created containing all its own transitive dependencies. The module descriptors
        |are used when, and if, the dependencies Jar files get imported into the Ceylon repository.
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
        if (project.hasProperty("ceylonModule")) project.property("ceylonModule").toString()
        else ""

    private val dependencies by lazy { ResolveCeylonDependencies(project, ceylonModule) }

    @InputFiles
    fun inputFiles() =
        project.allprojects.map { it.buildFile } + dependencies.moduleFile

    private val rootDir = getRootDir(project)
    @OutputDirectory
    fun outputDirectory() = listOf(rootDir)

    @TaskAction
    fun run() {
        dependencies.resolve().jarDependencies.forEach {
            val descriptor = descriptorTempLocation(it, project)
            if (!descriptor.exists()) descriptor.parentFile.mkdirs()
            ModuleDescriptorCreator.createModuleDescriptorFor(it, descriptor)
        }
    }
}