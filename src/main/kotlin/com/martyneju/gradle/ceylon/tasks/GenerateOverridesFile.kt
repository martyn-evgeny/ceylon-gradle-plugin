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
import java.io.IOException
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException

open class GenerateOverridesFile: DefaultTask() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """
            | Generates the overrides.xml file based on the Gradle project dependencies.
            | All Java legacy dependencies declared in the Ceylon module file are checked so
            | that if they require transitive dependencies, they are added to the auto-generated
            | overrides file.""".trimMargin()
    }

    val log = Logging.getLogger(GenerateOverridesFile::class.java)

    /**
     *   name of the Ceylon module
     *   mandatory if no Ceylon config file exists
     *   and no add properties with exec task
     */
    @Input
    var ceylonModule: String = if(project.hasProperty("ceylonModule")) project.property("ceylonModule").toString() else ""


    @InputFiles
    fun inputFiles() =
        project.allprojects.map { it.buildFile } + ResolveCeylonDependencies(project, ceylonModule).moduleFile

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

        val dependencyTree = ResolveCeylonDependencies(project, ceylonModule).resolve()
        writeOverridesFile(dependencyTree)
    }

    private fun writeOverridesFile(dependencyTree: DependencyTree) {
        val writer = XMLOutputFactory.newInstance().createXMLStreamWriter(overrides.outputStream(), "UTF-8")
        try {
            writer.document {
                element("overrides") {
                    attribute(
                        "xmlns",
                        "http://www.ceylon-lang.org/xsd/overrides"
                    )
                    dependencyTree.moduleDeclaration.imports.forEach {
                        element("set") {
                            attribute("module", it.name?:"")
                            attribute("version", it.version?:"")
                        }
                    }
                }
            }
            writer.flush()
        } catch (e: XMLStreamException) {
            MavenPomCreator.log.error(" error in create overrides.xml file ",e)
        } catch (e: IOException) {
            MavenPomCreator.log.error(" error in create overrides.xml file ",e)
        } finally {
            writer?.close()
        }
    }

}