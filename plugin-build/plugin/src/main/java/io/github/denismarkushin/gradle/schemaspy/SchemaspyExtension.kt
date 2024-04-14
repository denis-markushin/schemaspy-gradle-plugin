package io.github.denismarkushin.gradle.schemaspy

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
         * Postgres image which is used for docs generation. Default value is: **postgres:13.5-alpine**
         */
        val postgresDockerImage: Property<String> =
            objects.property(String::class.java)
                .convention("postgres:13.5-alpine")

        /**
         * SchemaSpy image which is used for docs generation. Default value is: **schemaspy/schemaspy:6.1.0**
         */
        val schemaspyDockerImage: Property<String> =
            objects.property(String::class.java)
                .convention("schemaspy/schemaspy:6.1.0")

        /**
         * Exclude tables regex. Default value is: "(databasechangeloglock|databasechangelog)"
         */
        val excludeTables: Property<String> =
            objects.property(String::class.java)
                .convention("(databasechangeloglock|databasechangelog)")

        /**
         * Should unzip output or not. Default value is: true
         */
        val unzipOutput: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    }
