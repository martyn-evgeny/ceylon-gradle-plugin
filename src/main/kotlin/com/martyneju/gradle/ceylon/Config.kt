package com.martyneju.gradle.ceylon

import com.martyneju.gradle.ceylon.utils.isWindows
import com.martyneju.gradle.ceylon.utils.lProperty
import com.martyneju.gradle.ceylon.utils.property
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

    val sourceRoots: ListProperty<String> = objects.lProperty<String>().convention( DefaultToolOptions.getCompilerSourceDirs().map { it.path } )
    val resourceRoots: ListProperty<String> = objects.lProperty<String>().convention( DefaultToolOptions.getCompilerResourceDirs().map { it.path })
    val testResourceRoots: ListProperty<String> = objects.lProperty<String>().convention(DefaultToolOptions.getCompilerResourceDirs().map { it.path })
    val testRoots: ListProperty<String> = objects.lProperty<String>().convention(DefaultToolOptions.getCompilerSourceDirs().map { it.path })
    val output: Property<String> = objects.property<String>().convention(DefaultToolOptions.getCompilerOutputRepo())

    val ceylonLocation: Property<String> = objects.property<String>().convention(File(GRADLE_FILES_DIR).resolve(CEYLON_ENVS_DIR).resolve("ceylon-1.3.3").resolve("bin").resolve("ceylon${ if(isWindows) ".bat" else ""}").path)
    val javaLocation: Property<String> = objects.property<String>().convention(File(GRADLE_FILES_DIR).resolve(CEYLON_ENVS_DIR).resolve("jdk8u292-b10").path)
    val overrides: Property<String> = objects.property<String>().convention(File("build").resolve("overrides.xml").path)
    val mavenSettings: Property<String> = objects.property<String>().convention(File("build").resolve("maven-settings.xml").path)
    val mavenRepo: Property<String> = objects.property<String>().convention(File("build").resolve("maven-repository").path)
    val fatJarDestination: Property<String> = objects.property<String>().convention(File("build").resolve("fatJar").path)
    val javaRuntimeDestination: Property<String> = objects.property<String>().convention(File("build").resolve("java-runtime").path)
    val testReportDestination: Property<String> = objects.property<String>().convention(File("build").resolve("reports").path)

    val flatClasspath: Property<Boolean> = objects.property<Boolean>().convention(config.getBoolOption( DefaultToolOptions.DEFAULTS_FLAT_CLASSPATH ) ?: true)
    val importJars: Property<Boolean> = objects.property<Boolean>().convention(false)
    val forceImports: Property<Boolean> = objects.property<Boolean>().convention(false)
    val verbose: Property<Boolean> = objects.property<Boolean>().convention(false)
    val generateTestReport: Property<Boolean> = objects.property<Boolean>().convention(true)
}
