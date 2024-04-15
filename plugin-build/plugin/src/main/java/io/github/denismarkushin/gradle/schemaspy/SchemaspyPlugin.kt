package io.github.denismarkushin.gradle.schemaspy

import org.gradle.api.Plugin
import org.gradle.api.Project

private const val EXTENSION_NAME = "schemaspyConfig"
private const val TASK_NAME = "generateSchemaspyDocs"

abstract class SchemaspyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, SchemaspyExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, SchemaspyTask::class.java) {
            it.dbName.set(extension.dbName)
            it.liquibaseChangelog.set(extension.liquibaseChangelog)
            it.excludeTables.set(extension.excludeTables)
            it.postgresDockerImage.set(extension.postgresDockerImage)
            it.schemaspyDockerImage.set(extension.schemaspyDockerImage)
            it.outputDir.set(project.schemaSpyOutputDir().dir(extension.dbName.get()))
            it.unzipOutput.set(extension.unzipOutput)
        }
    }

    private fun Project.schemaSpyOutputDir() = layout.buildDirectory.get().dir("schemaspy")
}
