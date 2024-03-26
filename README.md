# schemaspy-gradle-plugin

Gradle plugin that lets you generate database documentation using [Schemaspy](https://schemaspy.org)
and [Testcontainers](https://testcontainers.com)

## How to use 👣

1. Add dependency:
   ```kotlin
   id("org.dema.gradle.schemaspy.plugin")
   ```
2. Configure the plugin using extension.
   ```kotlin
   schemaspyConfig {
       dbName = "my_database"
       liquibaseChangelog = file("/path/to/file.yml")
       excludeTables = "(table1|table2)"
   }
   ```
3. Execute gradle `generateSchemaspyDocs` task:
   `gradle generateSchemaspyDocs`
4. Get output in gradle `build/schemaspy/output` folder. That`s it!

## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This project is licensed under the MIT License - see the [License](https://github.com/denis-markushin/schemaspy-gradle-plugin/blob/main/LICENSE) file for details.
