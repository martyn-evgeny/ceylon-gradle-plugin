package com.martyneju.gradle.ceylon.utils.dependency

import com.martyneju.gradle.ceylon.utils.dependency.CeylonModule
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvedDependency

/**
 * Tree of dependencies. The root of the tree will contain all project dependencies.
 */
class DependencyTree(val project: Project, moduleDeclaration: CeylonModule) {
    companion object {

        private fun accumulateDependencies(dependency: ResolvedDependency, acc: MutableMap<String, ResolvedDependency> ) {
            acc[dependency.name] = dependency
            dependency.children.forEach { accumulateDependencies(it, acc) }
        }

        fun transitiveDependenciesOf(dependency: ResolvedDependency): Collection<ResolvedDependency> {
            val depsById = mutableMapOf<String, ResolvedDependency>()
            dependency.children.forEach { accumulateDependencies(it, depsById) }
            return onlyJars( depsById.values )
        }

        fun directJarDependenciesOf(project: Project):Collection<ResolvedDependency> = onlyJars(directDependenciesOf(project))

        fun directCeylonDependenciesOf(project: Project): Collection<Project> =
            project
                .configurations
                .findByName("ceylonRuntime")
                ?.allDependencies
                ?.withType( ProjectDependency::class.java)
                ?.map { it.dependencyProject} ?: listOf()

        fun directDependenciesOf(project: Project): Collection<ResolvedDependency> =
            project
                .configurations
                .findByName("ceylonRuntime")
                ?.resolvedConfiguration
                ?.firstLevelModuleDependencies ?: listOf()

        fun directDependenciesOf(dependency: ResolvedDependency) = onlyJars(dependency.children)

        private fun onlyJars(dependencies: Collection<ResolvedDependency>): Collection<ResolvedDependency> =
            dependencies.filter { it.moduleArtifacts.any { artifact -> artifact.type == "jar"} }
    }

    val moduleName = moduleDeclaration.moduleName
    val moduleVersion =  moduleDeclaration.version
    val imports = moduleDeclaration.imports.filter { it.namespace == "maven" }
    val jarDependencies = accumulateDependenciesOf()
    val ceylonDependencies = directCeylonDependenciesOf(project)

    private fun accumulateDependenciesOf(): Collection<ResolvedDependency> {
        val depsById = mutableMapOf<String, ResolvedDependency>()
        directJarDependenciesOf(project).forEach { accumulateDependencies( it, depsById) }
        imports.forEach {
            val id = "${it.name}:${it.version}"
            if (depsById.containsKey( id )) {
                it.resolvedDependency = depsById[id]
            }
        }
        return onlyJars(depsById.values)
    }


    fun getModuleDeclaredDependencies() =
        onlyJars( imports.map { it.resolvedDependency }.filterNotNull())
    
    fun isShared( dependency: ResolvedDependency ) =
        moduleDeclaration( dependency )?.shared ?: false
    
    private fun moduleDeclaration( dependency: ResolvedDependency ) =
        imports.find {
            it.name == "${dependency.moduleGroup}:${dependency.moduleName}" &&
            it.version == dependency.moduleVersion
        }

}