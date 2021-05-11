package com.martyneju.gradle.ceylon

import com.martyneju.gradle.ceylon.tasks.Setup
import com.martyneju.gradle.ceylon.tasks.Clean
import com.martyneju.gradle.ceylon.tasks.GenerateOverridesFile
import org.gradle.api.Plugin
import org.gradle.api.Project

open class CeylonPlugin: Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        extensions.create(CEYLON_PLUGIN_EXTENSION_NAME, Config::class.java,project)

        val ceylonSetupTak = tasks.register("setupCeylon", Setup::class.java)
        tasks.register("cleanCeylon", Clean::class.java)
        tasks.register("generateOverridesFile", GenerateOverridesFile::class.java)

    }
}