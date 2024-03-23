plugins {
    java
    id("org.dema.gradle.schemaspy.plugin")
}

schemaspyConfig {
    dbName = "tickets"
}
