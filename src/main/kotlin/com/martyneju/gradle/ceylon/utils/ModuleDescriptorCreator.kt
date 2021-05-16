package com.martyneju.gradle.ceylon.utils

import com.martyneju.gradle.ceylon.utils.dependency.DependencyTree
import org.gradle.api.artifacts.ResolvedDependency
import java.io.File


class ModuleDescriptorCreator {
    companion object {
        fun createModuleDescriptorFor(dependency: ResolvedDependency, file: File) {
            file.writer().use { writer->
                val dependencies = DependencyTree.transitiveDependenciesOf(dependency)
                if (dependencies.isEmpty()) writer.write("") // create an empty file
                dependencies.forEach {
                    writer.write("+${it.moduleGroup}:${it.moduleName}=${it.moduleVersion}\n")
                }
            }
        }
    }
}