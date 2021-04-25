package com.martyneju.gradle.ceylon

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import com.redhat.ceylon.common.config.CeylonConfig
import com.redhat.ceylon.common.config.DefaultToolOptions
import org.gradle.api.provider.ListProperty
import java.io.File;

open class Config @Inject constructor(@Suppress("UNUSED_PARAMETER") project: Project,
                                      objects: ObjectFactory) {
    /**
     * Set the default values from the Ceylon config file, if it is present.
     */
    val config = CeylonConfig.get()

    val sourceRoots: ListProperty<File> = objects.lProperty<File>().convention( DefaultToolOptions.getCompilerSourceDirs() )
    val resourceRoots: ListProperty<File> = objects.lProperty<File>().convention( DefaultToolOptions.getCompilerResourceDirs())
    val testResourceRoots: ListProperty<File> = objects.lProperty<File>().convention(DefaultToolOptions.getCompilerResourceDirs())
    val testRoots: ListProperty<File> = objects.lProperty<File>().convention(DefaultToolOptions.getCompilerSourceDirs())
    val output: Property<String> = objects.property<String>().convention(DefaultToolOptions.getCompilerOutputRepo())
    val overrides: Property<String> = objects.property<String>().convention(project.buildDir.resolve("overrides.xml").path)
    val mavenSettings: Property<String> = objects.property<String>().convention(project.buildDir.resolve("maven-settings.xml").path)
    val flatClasspath: Property<Boolean> = objects.property<Boolean>().convention(config.getBoolOption( DefaultToolOptions.DEFAULTS_FLAT_CLASSPATH ) ?: true)
    val importJars: Property<Boolean> = objects.property<Boolean>().convention(false)
    val forceImports: Property<Boolean> = objects.property<Boolean>().convention(false)
    val verbose: Property<Boolean> = objects.property<Boolean>().convention(false)
    val javaRuntimeDestination: Property<String> = objects.property<String>().convention(project.buildDir.resolve("java-runtime").path)
    val fatJarDestination: Property<String> = objects.property<String>().convention(project.buildDir.resolve("fatJar").path)
    val generateTestReport: Property<Boolean> = objects.property<Boolean>().convention(true)
    val testReportDestination: Property<String> = objects.property<String>().convention(project.buildDir.resolve("reports").path)
}

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
