plugins {
    java
    id("org.dema.gradle.schemaspy.plugin")
}

schemaspyConfig {
    dbName = "tickets"
    liquibaseChangelog = file("${project.projectDir}/src/main/resources/liquibase/changelog-master.yml")
}
