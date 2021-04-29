package com.martyneju.gradle.ceylon.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.IOException

open class CeylonToolLocator {
    companion object {
        fun findCeylon(project: Project): String {
            val ceylon = project.ceylonPlugin.ceylonLocation.get().absolutePath
            try {
                Runtime.getRuntime().exec(ceylon).waitFor()
                return ceylon
            } catch(e: IOException) {
                throw GradleException("Ceylon could not be found! use task ceylonSetup or write in config <ceylonPlugin> parameter <ceylonLocation>" ,e)
            } catch(e: Throwable) {
                throw GradleException("A problem has occurred while trying to run the Ceylon tool at:" ,e)
            }
        }
    }
}