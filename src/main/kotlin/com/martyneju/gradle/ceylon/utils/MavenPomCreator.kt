package com.martyneju.gradle.ceylon.utils

import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.Logging
import java.io.FileOutputStream
import java.io.IOException
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

class MavenPomCreator {
    companion object {
        val log = Logging.getLogger( MavenPomCreator::class.java )

        fun createPomFor(dependency: ResolvedDependency,dependencies: Collection<ResolvedDependency>, fileName: String) {
            val writer = XMLOutputFactory.newInstance().createXMLStreamWriter(FileOutputStream(fileName), "UTF-8")

            try {
                writer.document {
                    element("project") {
                        attribute("xsi:schemaLocation","http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd")
                        attribute("xmlns","http://maven.apache.org/POM/4.0.0")
                        attribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance")
                    }
                    element("modelVersion","4.0.0")
                    element("groupId",dependency.moduleGroup)
                    element("artifactId",dependency.moduleName)
                    element("version",dependency.moduleVersion)

                    element("dependencies") {
                        dependencies.forEach {
                            element("dependency") {
                                element("groupId",it.moduleGroup)
                                element("artifactId",it.moduleName)
                                element("version", it.moduleVersion)
                            }
                        }
                    }
                }
                writer.flush()
            } catch (e: XMLStreamException) {
                log.error(" error in create maven pom file ",e)
            } catch (e: IOException) {
                log.error(" error in create maven pom file ",e)
            } finally {
                writer?.close()
            }
        }
    }
}
