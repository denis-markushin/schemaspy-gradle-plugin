package org.dema.gradle.schemaspy

import junit.framework.TestCase.fail
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.DirectoryResourceAccessor
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.rauschig.jarchivelib.ArchiverFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.Connection

abstract class SchemaspyTask : DefaultTask() {
    private companion object {
        const val DEFAULT_POSTGRES_IMAGE_NAME = "postgres:13.5-alpine"
        val POSTGRES_IMAGE = DockerImageName.parse(DEFAULT_POSTGRES_IMAGE_NAME).asCompatibleSubstituteFor("postgres")
        const val SCHEMASPY_IMAGE_NAME = "schemaspy/schemaspy:6.1.0"
        val SCHEMASPY_IMAGE = DockerImageName.parse(SCHEMASPY_IMAGE_NAME)
        var localNetwork = Network.newNetwork()
    }

    init {
        description = "Task to run DB in docker and generate output using SchemaSpy"
        group = "schemaspy"
    }

    @get:Input
    abstract val dbName: Property<String>

    @get:InputFile
    abstract val liquibaseChangelog: RegularFileProperty

    @get:Input
    abstract val excludeTables: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun sampleAction() {
        val postgresContainer =
            PostgreSQLContainer<Nothing>(POSTGRES_IMAGE).apply {
                withNetworkAliases("postgres")
                withNetwork(localNetwork)
                withDatabaseName(dbName.get())
            }

        postgresContainer.use { postgres ->
            postgres.start()
            postgres.createConnection("").use { connection ->
                val database = connection.database()

                val liquibase =
                    Liquibase(
                        liquibaseChangelog.get().asFile.name,
                        DirectoryResourceAccessor(liquibaseChangelog.get().asFile.toPath().parent),
                        database,
                    )

                @Suppress("DEPRECATION")
                liquibase.update()
            }

            GenericContainer<Nothing>(SCHEMASPY_IMAGE).apply {
                withNetworkAliases("schemaspy")
                withNetwork(localNetwork)
                withCreateContainerCmdModifier { it.withEntrypoint("") }
                withCommand("sleep 300000")
            }.use { schemaSpy ->
                schemaSpy.start()
                val generateDocCommand =
                    schemaSpy.execInContainer(
                        "java",
                        "-jar",
                        "/schemaspy-6.1.0.jar",
                        "-t",
                        "pgsql11",
                        "-db",
                        postgresContainer.databaseName,
                        "-host",
                        "postgres",
                        "-u",
                        postgresContainer.username,
                        "-p",
                        postgresContainer.password,
                        "-o",
                        "/output",
                        "-dp",
                        "/drivers_inc",
                        "-I",
                        excludeTables.get(),
                        "-debug",
                    )

                if (generateDocCommand.exitCode != 0) {
                    fail("Output: [${generateDocCommand.stdout}], error: [${generateDocCommand.stderr}]")
                }

                schemaSpy.execInContainer("tar", "-czvf", "/output/output.tar.gz", "/output")

                logger.lifecycle("outputDirectory is: ${outputDir.get()}")
                schemaSpy.copyFileFromContainer(
                    "/output/output.tar.gz",
                    "${outputDir.get()}/output.tar.gz",
                )
            }
        }

        val archiver = ArchiverFactory.createArchiver("tar", "gz")
        val outputArchive = outputDir.get().file("output.tar.gz").asFile

        archiver.extract(
            outputArchive,
            outputDir.get().asFile,
        )
        outputArchive.delete()
    }

    private fun Connection.database(): Database? =
        DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(this))
}
