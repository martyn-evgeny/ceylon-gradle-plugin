package com.martyneju.gradle.ceylon.utils

import com.martyneju.gradle.ceylon.CEYLON_ENVS_DIR
import com.martyneju.gradle.ceylon.Config
import com.martyneju.gradle.ceylon.GRADLE_FILES_DIR
import com.martyneju.gradle.ceylon.JAVA_ENVS_DIR
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.internal.os.OperatingSystem
import java.io.File


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

internal val Project.javaDir: File
    get() = this.ceylonDir.resolve(JAVA_ENVS_DIR)

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
            OperatingSystem.current().isMacOsX -> "MacOSX"
            isWindows -> "Windows"
            else -> "Linux"
        }
    }

/**
 * Returns system architecture name
 */
internal val arch: String
    get() {
        val arch = System.getProperty("os.arch")
        return when {
            OperatingSystem.current().isMacOsX -> "x86_64"
            isWindows -> when (arch) {
                "x86_64", "amd64" -> "x86_64"
                else -> "x86"
            }
            else -> "x86_64"
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
