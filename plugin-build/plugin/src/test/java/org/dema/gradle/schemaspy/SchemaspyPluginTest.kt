package org.dema.gradle.schemaspy

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SchemaspyPluginTest {
    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.dema.gradle.schemaspy.plugin")

        assert(project.tasks.getByName("schemaspy") is SchemaspyTask)
    }

    @Test
    fun `extension templateExampleConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.dema.gradle.schemaspy.plugin")

        assertNotNull(project.extensions.getByName("schemaspyConfig"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.dema.gradle.schemaspy.plugin")
        (project.extensions.getByName("schemaspyConfig") as SchemaspyExtension).apply {
            dbName.set("test_db_name")
            excludeTables.set("table_to_exclude")
        }

        val task = project.tasks.getByName("schemaspy") as SchemaspyTask

        assertEquals("test_db_name", task.dbName.get())
        assertEquals("table_to_exclude", task.excludeTables.get())
    }
}
