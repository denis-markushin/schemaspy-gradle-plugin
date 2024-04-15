plugins {
    java
    id("io.github.denis-markushin.schemaspy-plugin")
}

schemaspyConfig {
    dbName = "sakila"
    liquibaseChangelog = file("${project.projectDir}/src/main/resources/liquibase/changelog.yml")
}
