package com.martyneju.gradle.ceylon.utils

import org.gradle.api.artifacts.ResolvedDependency
import java.io.Writer

class ModuleDescriptorCreator {
    companion object {
        fun createModuleDescriptorFor(dependency: ResolvedDependency, dependencies: Collection<ResolvedDependency>, writer: Writer) {
            if(dependencies.isEmpty()) writer.write("") // create an empty file
            dependencies.forEach {
                writer.write("+${it.moduleGroup}:${it.moduleName}=${it.moduleVersion}\n")
            }
        }
    }
}