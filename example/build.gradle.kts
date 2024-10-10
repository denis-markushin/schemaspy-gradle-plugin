plugins {
    id("io.github.denis-markushin.schemaspy-plugin")
}

schemaspyConfig {
    dbName = "sakila"
    liquibaseChangelog = file("${project.projectDir}/src/main/resources/liquibase/changelog.yml")
    outputDir = project.layout.buildDirectory.dir("schemaspy/sakila")
}
