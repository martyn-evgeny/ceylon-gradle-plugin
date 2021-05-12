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

    fun <T: Task> addFilesFromTask(_class: Class<T>, acc: MutableCollection<Object>) {
        project.files(project.tasks.withType(_class)).from.forEach {
            acc.add(it as Object)
        }
    }

    fun filesToDelete(project: Project): List<Object> {
        val files = mutableListOf<Object>()
        files.add(project.buildDir!! as Object)
        files.add(MavenSettingsFileCreator.mavenSettingsFile(project) as Object)
        addFilesFromTask(GenerateOverridesFile::class.java, files)

        return files
    }

    @TaskAction
    override fun clean() {
        delete(filesToDelete(project))
        super.clean()
    }
}