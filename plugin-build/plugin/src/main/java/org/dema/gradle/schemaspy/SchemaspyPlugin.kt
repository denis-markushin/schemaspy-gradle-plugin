package org.dema.gradle.schemaspy

import org.gradle.api.Plugin
import org.gradle.api.Project

private const val EXTENSION_NAME = "schemaspyConfig"
private const val TASK_NAME = "generateSchemaspyDocs"

abstract class SchemaspyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, SchemaspyExtension::class.java, project)
        val outputDir = project.layout.buildDirectory.get().dir("schemaspy")

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, SchemaspyTask::class.java) {
            it.dbName.set(extension.dbName)
            it.liquibaseChangelog.set(extension.liquibaseChangelog)
            it.excludeTables.set(extension.excludeTables)
            it.outputDir.set(outputDir)
        }
    }
}
