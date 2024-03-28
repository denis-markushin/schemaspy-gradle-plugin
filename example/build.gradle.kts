plugins {
    java
    id("org.dema.gradle.schemaspy.plugin")
}

schemaspyConfig {
    dbName = "tickets"
    liquibaseChangelog = file("${project.projectDir}/src/main/resources/liquibase/changelog-master.yml")
    postgresDockerImage = "docker.samokat.io/dockerhub/postgres:12.8-alpine"
}
