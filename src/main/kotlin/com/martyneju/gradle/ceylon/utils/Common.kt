package com.martyneju.gradle.ceylon.utils

import com.martyneju.gradle.ceylon.CEYLON_ENVS_DIR
import com.martyneju.gradle.ceylon.Config
import com.martyneju.gradle.ceylon.GRADLE_FILES_DIR
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.internal.os.OperatingSystem
import java.io.File
import javax.xml.stream.XMLStreamWriter


/**
 * Creates a [Property] to hold values of the given type.
 *
 * @param T the type of the property
 * @return the property
 */
internal inline fun <reified T : Any> ObjectFactory.property(): Property<T> =
    property(T::class.javaObjectType)

/**
 * Creates a [ListProperty] to hold values of the given type.
 *
 * @param T the type of the property
 * @return the list property
 */
internal inline fun <reified T : Any> ObjectFactory.lProperty(): ListProperty<T> =
    listProperty(T::class.javaObjectType)

/**
 * Gets the [Config] that is installed on the project.
 */
internal val Project.ceylonPlugin: Config
    get() = extensions.getByType(Config::class.java)

/* =================== Files ============== */

internal val Project.ceylonDir: File
    get() = this.rootDir.resolve(GRADLE_FILES_DIR).resolve(CEYLON_ENVS_DIR)


/* =================== OS ============== */

/**
 * Returns if operating system is Windows
 */
internal val isWindows: Boolean
    get() {
        return OperatingSystem.current().isWindows
    }

/**
 * Returns simplified operating system name
 */
internal val os: String
    get() {
        return when {
            OperatingSystem.current().isMacOsX -> "mac"
            isWindows -> "windows"
            else -> "linux"
        }
    }

/**
 * Returns system architecture name
 */
internal val arch: String
    get() {
        val arch = System.getProperty("os.arch")
        return when {
            OperatingSystem.current().isMacOsX -> "x64"
            isWindows -> when (arch) {
                "x86_64", "amd64" -> "x64"
                else -> "x86-32"
            }
            else -> "x64"
        }
    }

/**
 * Returns exec extensions
 */
internal val exec: String
    get() {
        return when {
            OperatingSystem.current().isLinux -> "sh"
            isWindows -> "exe"
            else -> "sh"
        }
    }

/* =================== xml ==================== */

fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument("UTF-8", "1.0")
    this.init()
    this.writeEndDocument()
    return this
}

fun XMLStreamWriter.element(name: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.init()
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.element(name: String, content: String) {
    element(name) {
        writeCharacters(content)
    }
}

fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)