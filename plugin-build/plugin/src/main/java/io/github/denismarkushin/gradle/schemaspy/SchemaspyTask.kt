package io.github.denismarkushin.gradle.schemaspy

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
import org.gradle.api.tasks.Optional
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
        var localNetwork: Network = Network.newNetwork()
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

    @get:Input
    @get:Optional
    abstract val postgresDockerImage: Property<String>

    @get:Input
    @get:Optional
    abstract val schemaspyDockerImage: Property<String>

    @get:Input
    @get:Optional
    abstract val unzipOutput: Property<Boolean>

    @get:OutputDirectory
    @get:Optional
    abstract val outputDir: DirectoryProperty

    private val postgresImage by lazy {
        DockerImageName.parse(postgresDockerImage.get()).asCompatibleSubstituteFor("postgres")
    }

    private val schemaspyImage by lazy { DockerImageName.parse(schemaspyDockerImage.get()) }

    @TaskAction
    fun generateDocs() {
        val postgresContainer =
            PostgreSQLContainer<Nothing>(postgresImage).apply {
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

            startSchemaspyContainerAndRunGenerate(postgresContainer)
        }

        if (unzipOutput.get()) {
            unzipOutput()
        }
    }

    private fun Connection.database(): Database? =
        DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(this))

    private fun startSchemaspyContainerAndRunGenerate(postgresContainer: PostgreSQLContainer<Nothing>) {
        GenericContainer<Nothing>(schemaspyImage).apply {
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
                    "/schemaspy-7.0.2.jar",
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

    private fun unzipOutput() {
        val archiver = ArchiverFactory.createArchiver("tar", "gz")
        val outputArchive = outputDir.get().file("output.tar.gz").asFile

        archiver.extract(outputArchive, outputDir.get().asFile)
        outputArchive.delete()
    }
}
