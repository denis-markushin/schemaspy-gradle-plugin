package org.dema.gradle.schemaspy

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class SchemaspyExtension
@Inject
constructor(project: Project) {
    private val objects = project.objects

    /**
     * Database name
     */
    val dbName: Property<String> = objects.property(String::class.java)

    /**
     * Liquibase changelog file path
     */
    val liquibaseChangelog: RegularFileProperty = objects.fileProperty()

    /**
     * Exclude tables regex. Default value is: "(databasechangeloglock|databasechangelog)"
     */
    val excludeTables: Property<String> = objects.property(String::class.java)
        .convention("(databasechangeloglock|databasechangelog)")
}
