package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.*
import com.martyneju.gradle.ceylon.utils.ceylonPlugin
import com.martyneju.gradle.ceylon.utils.dependency.DependencyTree
import com.martyneju.gradle.ceylon.utils.dependency.ResolveCeylonDependencies
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 *  https://ceylon-lang.org/documentation/1.3/reference/repository/overrides/
 *
 */
open class GenerateOverridesFile: DefaultTask() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """
            |Generates the overrides.xml file based on the Gradle project dependencies.
            |All Java legacy dependencies declared in the Ceylon module file are checked so
            |that if they require transitive dependencies, they are added to the auto-generated
            |overrides file.""".linear()
    }

    val log = Logging.getLogger(GenerateOverridesFile::class.java)

    /**
     *   name of the Ceylon module
     *   mandatory if no Ceylon config file exists
     *   and no add properties with exec task
     */
    @Input
    var ceylonModule: String = if(project.hasProperty("ceylonModule")) project.property("ceylonModule").toString() else ""

    private val dependencies by lazy { ResolveCeylonDependencies(project, ceylonModule)}
    @InputFiles
    fun inputFiles() =
        project.allprojects.map { it.buildFile } + dependencies.moduleFile

    private val overrides = project.file(project.ceylonPlugin.overrides.get())

    @OutputFiles
    fun outputFiles() = listOf(overrides)

    @TaskAction
    fun run() {
        if(overrides.exists()) overrides.delete()
        overrides.parentFile.mkdirs()

        if(!overrides.parentFile.isDirectory)
            throw GradleException("Directory of overrides.xml file does not exist and could not be created. Check access rights to this location: ${overrides.parentFile.absolutePath}")

        log.info("Generating Ceylon overrides.xml file at ${overrides.absolutePath}")

        val dependencyTree = dependencies.resolve()
        writeOverridesFile(dependencyTree)
    }

    private fun writeOverridesFile(dependencyTree: DependencyTree) {
        prettyPrinting(overrides) {
            it.document {
                element("overrides") {
                    attribute(
                        "xmlns",
                        "http://www.ceylon-lang.org/xsd/overrides"
                    )
                    dependencyTree.moduleDeclaration.imports.filter { it.namespace != "maven" }.forEach {
                        element("set") {
                            attribute("module", it.name?:"")
                            attribute("version", it.version?:"")
                        }
                    }
                    dependencyTree.getModuleDeclaredDependencies().forEach {
                        val transitiveDeps = DependencyTree.transitiveDependenciesOf(it)
                        if(transitiveDeps.isNotEmpty()) {
                            element("artifact") {
                                attribute("groupId", it.moduleGroup)
                                attribute("artifactId", it.moduleName)
                                attribute("version", it.moduleVersion)
                                if(dependencyTree.isShared(it)) attribute("shared ", "true")
                                transitiveDeps.forEach {
                                    element("add") {
                                        attribute("groupId", it.moduleGroup)
                                        attribute("artifactId", it.moduleName)
                                        attribute("version", it.moduleVersion)
                                        attribute("shared ", "true")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}