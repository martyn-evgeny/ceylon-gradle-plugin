package com.martyneju.gradle.ceylon.tasks

import com.martyneju.gradle.ceylon.PLUGIN_TASKS_GROUP_NAME
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.gradle.util.GFileUtils
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

@Deprecated("need change")
open class OfflineMavenRepository: DefaultTask() {
    init {
        group = PLUGIN_TASKS_GROUP_NAME
        description = """create local offline maven repository """
    }

    @Input
    val configurationName = "implementation"

    @OutputDirectory
    val repoDir = project.file("dep").resolve("m2").resolve("repository")

    @TaskAction
    fun build() {
        if(!repoDir.exists()) repoDir.mkdirs()
        val configuration = project.configurations.getByName(configurationName)
        copyJars(configuration)
        copyPoms(configuration)
    }

    private fun copyJars(configuration: Configuration) {
        configuration.resolvedConfiguration.resolvedArtifacts.forEach {
            val id = it.moduleVersion.id
            val moduleDir = id
                .group.
                split(".")
                .fold(repoDir) { acc, name ->  acc.resolve(name) }
                .resolve(id.name)
                .resolve(id.version)
            GFileUtils.mkdirs(moduleDir)
            GFileUtils.copyFile(it.file, moduleDir.resolve(it.file.name))
        }
    }

    private fun copyPoms(configuration: Configuration) {
        val componentIds = configuration.incoming.resolutionResult.allDependencies.map { it.from.id }

        val result = project.dependencies.createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

        result.resolvedComponents.forEach {
            val id = it.id
            if (id is ModuleComponentIdentifier) {
                val moduleDir = id
                    .group.
                    split(".")
                    .fold(repoDir) { acc, name ->  acc.resolve(name) }
                    .resolve(id.module)
                    .resolve(id.version)
                GFileUtils.mkdirs(moduleDir)
                val pomFile = (it.getArtifacts(MavenPomArtifact::class.java).elementAt(0) as ResolvedArtifactResult).file
                GFileUtils.copyFile(pomFile, moduleDir.resolve(pomFile.name))
            }
        }
    }
}