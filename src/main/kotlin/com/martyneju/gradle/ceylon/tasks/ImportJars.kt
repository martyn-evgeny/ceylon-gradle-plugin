package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.CeylonCommandOptions
import com.martyneju.gradle.ceylon.utils.CeylonRunner
import com.martyneju.gradle.ceylon.utils.ceylonPlugin
import com.martyneju.gradle.ceylon.utils.dependency.ResolveCeylonDependencies
import com.martyneju.gradle.ceylon.utils.linear
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.streams.SafeStreams
import java.io.InputStream
import java.io.OutputStream

open class ImportJars: DefaultTask() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """
            |Import transitive Maven dependencies and copies the output from dependent Ceylon
            |projects into the local Ceylon repository.
            |To enable importing Maven dependencies, the Ceylon config property "importJars" must
            |be set to true.""".linear()
    }

    val log = Logging.getLogger(GenerateOverridesFile::class.java)

    /**
     *   name of the Ceylon module
     *   mandatory if no Ceylon config file exists
     *   and no add properties with exec task
     */
    @Input
    var ceylonModule: String = if(project.hasProperty("ceylonModule")) project.property("ceylonModule").toString() else ""

    private val dependencies by lazy { ResolveCeylonDependencies(project, ceylonModule) }
    @InputFiles
    fun inputFiles() =
        project.allprojects.map { it.buildFile } + dependencies.moduleFile

    private val repoDir = project.file(project.ceylonPlugin.output.get())
    @OutputDirectory
    fun outputDirectory() = listOf(repoDir)

    @Input
    var standardInput: InputStream = SafeStreams.emptyInput()

    @Input
    var standardOutput: OutputStream = SafeStreams.systemOut()

    @TaskAction
    fun run() {
        log.debug("Importing artifact jars")
        if ( !repoDir.isDirectory && !repoDir.mkdirs() ) {
            throw GradleException( "Output repository does not exist and cannot be created ${repoDir.absolutePath}." )
        }
        val depTree = dependencies.resolve()
        if(project.ceylonPlugin.importJars.get()) {
            log.info("Importing Jar dependencies in ${repoDir}")
            depTree.jarDependencies.forEach {
                importDependecy(it)
            }
        }

        log.info("Importing Ceylon dependencies")
        depTree.ceylonDependencies.forEach {
            importCeylonProject(it)
        }
    }

    private fun importCeylonProject(dependency: Project) {
        log.info("Trying to import dependency ${dependency.name} as a Ceylon project")
        val dependencyOutput = dependency.file(dependency.ceylonPlugin.output)
        if(dependencyOutput.isDirectory) {
            log.info("Copying output from $dependencyOutput to $repoDir")
            project.copy {
                it.into(repoDir)
                it.from(dependencyOutput)
            }
        } else {
            log.info("Dependency ${dependency.name} is not a Ceylon project")
        }
    }

    private fun importDependecy( dependency: ResolvedDependency) {
        val jar = dependency.allModuleArtifacts.find { it.type == "jar" }
        if(jar != null) {
            if(jar.name == dependency.moduleName) {
                importJar(dependency, jar)
            } else {
                log.warn("Unable to install dependency. Module name ${dependency.moduleName} != Jar name ${jar.name}")
            }
        } else {
            log.info("Dependency ${jar?.name} will not be installed as it has no jar file")
        }
    }

    private fun importJar(dependency: ResolvedDependency, jar: ResolvedArtifact) {
        log.info("will try to install ${jar.name} into the Ceylon repository ${repoDir}")
        var module = "${dependency.moduleGroup}.${dependency.moduleName}/${dependency.moduleVersion}"
        if(module.contains("-")) {
            log.warn("Importing module with illegal character '-' in name: $module. Will replace '-' with '_'.")
            module = module.replace( '-', '_' )
        }
        val moduleDescriptor = CreateModuleDescriptors.descriptorTempLocation(dependency, project)
        val jarFile = jar.file
        if (jarFile.exists()) {
            log.debug( "Jar: ${jarFile.name}, Module Name: ${module}" )
            CeylonRunner.run("import-jar",module,project,
                CeylonCommandOptions.getImportJarsOptions(project, moduleDescriptor),
                listOf(jarFile.absolutePath),
                standardInput,
                standardOutput
            )
        } else {
            throw GradleException(
                """|Dependency ${module} could not be installed in the Ceylon Repository
                   |because its jarFile could not be located: ${jarFile}""".linear()
            )
        }
    }

}