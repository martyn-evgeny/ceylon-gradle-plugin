package com.martyneju.gradle.ceylon

import org.gradle.api.Plugin
import org.gradle.api.Project

open class CeylonPlugin: Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        extensions.create(CEYLON_PLUGIN_EXTENSION_NAME, Config::class.java,project)

    }
}