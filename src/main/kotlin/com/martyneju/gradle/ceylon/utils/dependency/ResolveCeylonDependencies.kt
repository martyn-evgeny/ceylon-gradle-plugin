package com.martyneju.gradle.ceylon.utils.dependency

import com.martyneju.gradle.ceylon.utils.ceylonPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import java.io.File
import com.redhat.ceylon.common.Backend
import com.redhat.ceylon.common.config.DefaultToolOptions

class ResolveCeylonDependencies(val project: Project, _moduleName: String) {
    val moduleNameFromFile = DefaultToolOptions.getCompilerModules( project.ceylonPlugin.config, Backend.Header ).joinToString( ".")
    val module: String? = if(_moduleName!="") _moduleName else if (moduleNameFromFile!="*") moduleNameFromFile else null
    val log = Logging.getLogger( ResolveCeylonDependencies::class.java)
    val moduleFile = moduleFile()

    fun getModuleName() = module ?: ""

    fun resolve(): DependencyTree {
        if(!moduleFile.isFile)
            throw GradleException("""
            Ceylon module file does not exist.
            Please make sure that you set "sourceRoot" and "module"
            correctly in the "ceylon" configuration."""".trimIndent())

        val moduleDeclaration = CeylonModuleParser(module!!).parse(moduleFile.readText())
        val mavenDependencies = moduleDeclaration.imports.filter { it.namespace == "maven" }

        val existingDependencies = project
            .configurations
            .getByName("ceylonCompile")
            .dependencies
            .stream()
            .map {
                "${it.group}:${it.name}:${it.version}"
            }.toArray().toList()

        log.debug("Project existing dependencies: ${existingDependencies}")

        mavenDependencies.forEach {
            if(!existingDependencies.contains("${it.name}:${it.version}")) {
                addMavenDependency(it)
            } else {
                log.info("Not adding transitive dependencies of module ${it} as it already existed in the project")
            }
        }

        val dependencyTree = DependencyTree(project, moduleDeclaration)
        log.info("dependency found correctly")

        return dependencyTree
    }

    fun moduleFile(): File {
        if( module == null) {
           log.error("""
            | The Ceylon module name has not been specified.
            | To specify the name of your Ceylon module, using parameter "ceylonModule"
            |  
            | If you prefer, you can set the default module in the Ceylon config file instead,
            | and that will be used by Gradle. Or you can add properties when exec task: -PceylonModule="com.example.name.ceylon.module" 
           """.trimMargin())
           throw GradleException("The Ceylon module must be specified")
        }

        val moduleNameParts = module.split(".")
        val locations = mutableListOf<String>()
        project.ceylonPlugin.sourceRoots.get().forEach {
            val moduleName = moduleNameParts
                .fold(project.file(it)) { acc, s ->  acc.resolve(s)}
                .resolve("module.ceylon")
            locations.add(moduleName.absolutePath)
            if (moduleName.exists()) return moduleName
        }
        throw GradleException("Module file cannot be located. Looked at the following locations: $locations")
    }

    fun addMavenDependency(dependency: CeylonImport) {
        log.info("Adding dependency: ${dependency.name}:${dependency.version}")
        project.dependencies.add("ceylonCompile", "${dependency.name}:${dependency.version}")
    }

}
