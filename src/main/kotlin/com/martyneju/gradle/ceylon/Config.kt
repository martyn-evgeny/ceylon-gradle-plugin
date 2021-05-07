package com.martyneju.gradle.ceylon

import com.martyneju.gradle.ceylon.utils.*
import com.martyneju.gradle.ceylon.utils.ceylonDir
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

    val sourceRoots: ListProperty<File> = objects.lProperty<File>().convention( DefaultToolOptions.getCompilerSourceDirs() )
    val resourceRoots: ListProperty<File> = objects.lProperty<File>().convention( DefaultToolOptions.getCompilerResourceDirs())
    val testResourceRoots: ListProperty<File> = objects.lProperty<File>().convention(DefaultToolOptions.getCompilerResourceDirs())
    val testRoots: ListProperty<File> = objects.lProperty<File>().convention(DefaultToolOptions.getCompilerSourceDirs())
    val output: Property<String> = objects.property<String>().convention(DefaultToolOptions.getCompilerOutputRepo())

    val ceylonLocation: Property<File> = objects.property<File>().convention(project.ceylonDir.resolve("ceylon-1.3.3").resolve("bin").resolve("ceylon${ if(isWindows) ".bat" else ""}"))
    val javaLocation: Property<File> = objects.property<File>().convention(project.javaDir.resolve("jdk8u292-b10"))
    val overrides: Property<File> = objects.property<File>().convention(project.buildDir.resolve("overrides.xml"))
    val mavenSettings: Property<File> = objects.property<File>().convention(project.buildDir.resolve("maven-settings.xml"))
    val mavenRepo: Property<File> = objects.property<File>().convention(project.buildDir.resolve("maven-repository"))
    val fatJarDestination: Property<File> = objects.property<File>().convention(project.buildDir.resolve("fatJar"))
    val javaRuntimeDestination: Property<File> = objects.property<File>().convention(project.buildDir.resolve("java-runtime"))
    val testReportDestination: Property<File> = objects.property<File>().convention(project.buildDir.resolve("reports"))

    val flatClasspath: Property<Boolean> = objects.property<Boolean>().convention(config.getBoolOption( DefaultToolOptions.DEFAULTS_FLAT_CLASSPATH ) ?: true)
    val importJars: Property<Boolean> = objects.property<Boolean>().convention(false)
    val forceImports: Property<Boolean> = objects.property<Boolean>().convention(false)
    val verbose: Property<Boolean> = objects.property<Boolean>().convention(false)
    val generateTestReport: Property<Boolean> = objects.property<Boolean>().convention(true)
}
