# sbt-jooq

[![CI](https://github.com/kxbmap/sbt-jooq/actions/workflows/ci.yml/badge.svg)](https://github.com/kxbmap/sbt-jooq/actions/workflows/ci.yml)

jOOQ plugin for sbt 1.3+


## JooqCodegenPlugin

The plugin for easy use of jOOQ-codegen.

### Installation

Add the following to `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.kxbmap" % "sbt-jooq-codegen" % "0.7.0")
```

Then in `build.sbt`:

```scala
// Enable the plugin
enablePlugins(JooqCodegenPlugin)

// Add a driver dependency for the database you are using to "jooq-codegen" scope
libraryDependencies += "com.h2database" % "h2" % "1.4.200" % JooqCodegen
```

---
### Tasks

#### jooqCodegen

Run jOOQ-codegen according to settings.

#### jooqCodegenIfAbsent

Run jOOQ-codegen according to settings if generated files absent.

If generated files present, there is no effect.

---
### Settings

#### jooqVersion

- Optional (but recommended)
- Default: `"3.15.4"`

Version of the jOOQ libraries to be used.

```scala
jooqVersion := "3.15.4"
```

#### jooqOrganization

- Optional
- Default: `"org.jooq"`

Organization/GroupID of jOOQ libraries.

If you want to use the commercial version of jOOQ, set the appropriate one. For details, please refer to
the [jOOQ manual](https://www.jooq.org/doc/3.15/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step1/).

```scala
jooqOrganization := "org.jooq"
```

#### autoJooqLibrary

- Optional
- Default: `true`

Add jOOQ dependencies automatically if true.

If you want to manage jOOQ dependencies yourself, set this flag to false.

```scala
autoJooqLibrary := true
```

#### jooqCodegenConfig

- Required
- Default: `empty`

Configuration of jOOQ-codegen.

This is empty by default and must be set. You can set a file, classpath resource, XML directly, or multiple of them.

```scala
// Set a file
// Note: Relative path is resolved from the root of the project.
jooqCodegenConfig := file("path/to/jooq-codegen.xml")
```

```scala
// Set a URI of classpath resource
// Note: Resources are loaded from the `jooq-codegen` scope.
//       (e.g. resources under the `./src/jooq-codegen/resources/` directory)
jooqCodegenConfig := uri("classpath:path/to/jooq-codegen.xml")
```

```scala
// Set XML directly using XML literal
jooqCodegenConfig :=
  <configuration>
    <!-- Your configurations -->
  </configuration>
```

```scala
// Set multiple configurations
jooqCodegenConfig := Seq[CodegenConfig](
  file("path/to/jooq-codegen.xml"),
  uri("classpath:path/to/jooq-codegen.xml"),
  <configuration>
    <!-- Your configurations -->
  </configuration>
)

// Append a configuration
jooqCodegenConfig += file("append1.xml")

// Append configurations
jooqCodegenConfig ++= Seq(file("append2.xml"), file("append3.xml"))
```

#### jooqCodegenVariables

- Optional
- Default: `empty`

Variables for configuration placeholders replacement.

```scala
jooqCodegenVariables := Map.empty
```

#### jooqCodegenMode

- Optional
- Default: `CodegenMode.Auto`

Mode of jOOQ-codegen execution.

|CodegenMode  |Before each compile            |Destination directory                          |
|-------------|-------------------------------|-----------------------------------------------|
|`Auto`       |Run `jooqCodegenIfAbsent` task |target/scala-<SCALA_VERSION>/src_managed/main/ |
|`Always`     |Run `jooqCodegen` task         |target/scala-<SCALA_VERSION>/src_managed/main/ |
|`Unmanaged`  |Do nothing                     |src/main/jooq-generated/                       |

`Auto` and `Always` are "Managed" mode that runs generation task before each compile and places
generated files to the managed directory.

`Unmanaged` mode do nothing before compile. Therefore, you need to run generation task manually.
Also, generated files are placed in the unmanaged directory.

If you want to change the destination directory, set to `Compile / jooqSource` key.

```scala
jooqCodegenMode := CodegenMode.Auto

Compile / jooqSource := crossTarget.value / "jooq-generated"
```

---
### Rewriting configurations

The plugin rewrites configurations before code generation.

#### Rewrite rules

- Replace placeholders like `${KEY}` in configurations to variables' value.

  - If value is `java.util.Properties`, it expands to property list of key/value pairs, otherwise to string.

- If the `generator \ target \ directory` element is not configured, append it using `jooqSource` value.

#### Example

Define variables in build:

```scala
jooqCodegenVariables ++= Map(
  "JDBC_DRIVER" -> "com.mysql.cj.jdbc.Driver",
  "JDBC_URL" -> "jdbc:mysql://localhost/testdb",
  "JDBC_PROPS" -> {
    val props = new java.util.Properties()
    props.setProperty("user", "root")
    props.setProperty("password", "secret")
    props
  },
)
```

And configuration is below:

```xml
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-3.15.0.xsd">
    <jdbc>
        <driver>${JDBC_DRIVER}</driver>
        <url>${JDBC_URL}</url>
        <properties>${JDBC_PROPS}</properties>
    </jdbc>
    <generator>
        <target>
            <packageName>com.example</packageName>
        </target>
    </generator>
</configuration>
```

Then rewrites to:

```xml
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-3.15.0.xsd">
    <jdbc>
        <driver>com.mysql.cj.jdbc.Driver</driver>
        <url>jdbc:mysql://localhost/testdb</url>
        <properties>
            <property>
                <key>user</key>
                <value>root</value>
            </property>
            <property>
                <key>password</key>
                <value>secret</value>
            </property>
        </properties>
    </jdbc>
    <generator>
        <target>
            <packageName>com.example</packageName>
            <directory>/path/to/project/target/scala-2.13/src_managed/main</directory>
        </target>
    </generator>
</configuration>
```

---
## License

Copyright 2015 Tsukasa Kitachi

Apache License, Version 2.0
