package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import com.martyneju.gradle.ceylon.utils.MavenSettingsFileCreator
import org.gradle.api.tasks.Delete
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

open class Clean: Delete() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = "Removes the output of all tasks of the Ceylon plugin"
    }

    private fun addFilesFromTask(acc: MutableCollection<Object>, vararg _class: Class<*>) {
        _class.forEach {
            project.files(project.tasks.withType(it as Class<Task>)).from.forEach { file ->
                acc.add(file as Object)
            }
        }
    }

    fun filesToDelete(project: Project): List<Object> {
        val files = mutableListOf<Object>()
        files.add(project.buildDir!! as Object)
        files.add(MavenSettingsFileCreator.mavenSettingsFile(project) as Object)
        addFilesFromTask( files,
            GenerateOverridesFile::class.java,
            CreateDependenciesPoms::class.java,
            CreateMavenRepo::class.java,
            CreateModuleDescriptors::class.java,
            ImportJars::class.java,
            CompileCeylon::class.java
        )

        return files
    }

    @TaskAction
    override fun clean() {
        delete(filesToDelete(project))
        super.clean()
    }
}