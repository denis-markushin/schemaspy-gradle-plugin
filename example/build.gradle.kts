plugins {
    java
    id("io.github.denis-markushin.schemaspy-plugin")
}

schemaspyConfig {
    dbName = "tickets"
    liquibaseChangelog = file("${project.projectDir}/src/main/resources/liquibase/changelog-master.yml")
}
