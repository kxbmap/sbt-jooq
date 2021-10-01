# sbt-jooq

[![CI](https://github.com/kxbmap/sbt-jooq/actions/workflows/ci.yml/badge.svg)](https://github.com/kxbmap/sbt-jooq/actions/workflows/ci.yml)

jOOQ plugin for sbt 1.3+

```scala mdoc:invisible
import sbt._
import sbt.Keys._
import sbtjooq.JooqPlugin.autoImport._
import sbtjooq.codegen.JooqCodegenPlugin
import sbtjooq.codegen.JooqCodegenPlugin.autoImport._
```

## JooqCodegenPlugin

The plugin for easy use of jOOQ-codegen.

Automatically performs code generation at compile time.

### Installation

Add the following to your `project/plugins.sbt`:

```scala mdoc:compile-only
addSbtPlugin("com.github.kxbmap" % "sbt-jooq-codegen" % "@VERSION@")
```

Then in your `build.sbt`:

```scala mdoc:compile-only
// Enable the plugin
enablePlugins(JooqCodegenPlugin)

// Add your database driver dependency to `jooq-codegen` scope
libraryDependencies += "com.h2database" % "h2" % "@H2_VERSION@" % JooqCodegen
```

### Tasks

#### jooqCodegen

Run jOOQ-codegen manually according to configurations.

#### jooqCodegenIfAbsent

Run jOOQ-codegen manually if generated files absent.

If generated files present, there is no effect.

### Settings

#### jooqVersion

Default: `"@JOOQ_VERSION@"`

Version of jOOQ library to use.

##### Example

```scala mdoc:compile-only
jooqVersion := "@JOOQ_VERSION@"
```

#### jooqOrganization

Default: `"org.jooq"`

jOOQ organization/group ID.

If you want to use a commercial version of jOOQ, set appropriate one and install jOOQ to local.

For details, please refer to the [jOOQ manual](https://www.jooq.org/doc/@JOOQ_MINOR_VERSION@/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step1/).

##### Example

```scala mdoc:compile-only
jooqOrganization := "org.jooq.trial"
```

#### autoJooqLibrary

Default: `true`

Add jOOQ dependencies automatically if true.

If you want to manage jOOQ dependencies manually, set this flag to false.

##### Example

```scala mdoc:compile-only
autoJooqLibrary := true
```

#### jooqCodegenConfig

Default: `empty`

Configuration of jOOQ-codegen.

This is empty by default and must be set. You can set a file, classpath resource, XML directly, or multiple of them.

##### Example: Set a file

```scala mdoc:compile-only
jooqCodegenConfig := file("path/to/jooq-codegen.xml")
```

Note: relative paths are resolved from the root of the project.

##### Example: Set a URI of classpath resource

```scala mdoc:compile-only
jooqCodegenConfig := uri("classpath:path/to/jooq-codegen.xml")
```

Note: resources are loaded from the `jooq-codegen` scope. (e.g. under the `./src/jooq-codegen/resources/` directory)

##### Example: Set XML directly using literal

```scala mdoc:compile-only
jooqCodegenConfig :=
  <configuration>
    <!-- Your configurations -->
  </configuration>
```

##### Example: Set multiple configurations

```scala mdoc:compile-only
// Set configurations
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

#### jooqCodegenAutoStrategy

Default: `AutoStrategy.IfAbsent`

Auto-generation strategy at compile time.

|Value      |Description                                    |
|-----------|-----------------------------------------------|
|`IfAbsent` |Execute if files in target directory absent    |
|`Always`   |Always execute                                 |
|`Never`    |Never execute                                  |

##### Example

```scala mdoc:compile-only
jooqCodegenAutoStrategy := AutoStrategy.Always
```

#### jooqCodegenVariables

Default:

|KEY                |VALUE (example)                                |
|-------------------|-----------------------------------------------|
|TARGET_DIRECTORY   |/path/to/target/scala-2.13/src_managed/main    |
|RESOURCE_DIRECTORY |/path/to/src/main/jooq-codegen/resources       |

Variables for configuration text replacement.

The plugin replaces placeholders like `${KEY}` in configuration to `VALUE`.

##### Example

```scala mdoc:compile-only
jooqCodegenVariables ++= Map(
  "JDBC_DRIVER" -> "org.h2.Driver",
  "JDBC_URL" -> "jdbc:h2:path/to/database",
)
```

If configuration file contains some placeholders:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-@JOOQ_MINOR_VERSION@.0.xsd">
    <jdbc>
        <driver>${JDBC_DRIVER}</driver>
        <url>${JDBC_URL}</url>
    </jdbc>
    <generator>
        <target>
            <packageName>com.example</packageName>
            <directory>${TARGET_DIRECTORY}</directory>
        </target>
    </generator>
</configuration>
```

Then replace them before code generation:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-@JOOQ_MINOR_VERSION@.0.xsd">
    <jdbc>
        <driver>org.h2.Driver</driver>
        <url>jdbc:h2:path/to/database</url>
    </jdbc>
    <generator>
        <target>
            <packageName>com.example</packageName>
            <directory>/path/to/target/scala-2.13/src_managed/main</directory>
        </target>
    </generator>
</configuration>
```

## License

Copyright 2015-2018 Tsukasa Kitachi

Apache License, Version 2.0
