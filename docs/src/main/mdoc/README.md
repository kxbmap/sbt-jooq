# sbt-jooq

[![CI](https://github.com/kxbmap/sbt-jooq/actions/workflows/ci.yml/badge.svg)](https://github.com/kxbmap/sbt-jooq/actions/workflows/ci.yml)

jOOQ plugin for sbt 1.0+

```scala mdoc:invisible
import sbt._
import sbt.Keys._
import sbtjooq.JooqPlugin.autoImport._
import sbtjooq.codegen.JooqCodegenPlugin
import sbtjooq.codegen.JooqCodegenPlugin.autoImport._
```

## JooqCodegenPlugin

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

Run jOOQ-codegen according to configurations.

#### jooqCodegenIfAbsent

Run jOOQ-codegen if generated files absent.
If generated files present, there is no effect.

### Settings

#### jooqVersion
Version of jOOQ library.

Default: `"@JOOQ_VERSION@"`

```scala mdoc:compile-only
jooqVersion := "@JOOQ_VERSION@"
```

#### jooqOrganization
jOOQ organization/group ID.

If you want to use a commercial version of jOOQ, set appropriate one.
For details, please refer to the [jOOQ manual](https://www.jooq.org/doc/@JOOQ_MINOR_VERSION@/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step1/).

Default: `"org.jooq"`

```scala mdoc:compile-only
jooqOrganization := "org.jooq"
```

#### autoJooqLibrary
Add jOOQ dependencies automatically if true.

If you want to manage jOOQ dependencies manually, set this flag to false.

Default: `true`

```scala mdoc:compile-only
autoJooqLibrary := true
```

#### jooqCodegenConfig
jOOQ-codegen configuration.
This is empty by default and must be set. You can set a file, classpath resource, XML directly, or sequence of these.

```scala mdoc:compile-only
// Set file path
jooqCodegenConfig := file("path/to/jooq-codegen.xml")
```

```scala mdoc:compile-only
// Set URI of classpath resource
jooqCodegenConfig := uri("classpath:path/to/jooq-codegen.xml")
```

```scala mdoc:compile-only
// Set XML configuration
jooqCodegenConfig :=
  <configuration>
    <!-- Your configurations -->
  </configuration>
```

```scala mdoc:compile-only
// Seq of configurations
jooqCodegenConfig := Seq[CodegenConfig](
  file("path/to/jooq-codegen.xml"),
  uri("classpath:path/to/jooq-codegen.xml"),
  <configuration>
    <!-- Your configurations -->
  </configuration>
)
```

#### jooqCodegenAutoStrategy
jOOQ-codegen auto execution strategy.

|Value      |Description                                              |
|-----------|---------------------------------------------------------|
|`IfAbsent` |Execute if absent files in jOOQ-codegen target directory |
|`Always`   |Always execute                                           |
|`Never`    |Never execute                                            |

Default: `AutoStrategy.IfAbsent`

```scala mdoc:compile-only
jooqCodegenAutoStrategy := AutoStrategy.IfAbsent
```

#### jooqCodegenVariables
Variables for jOOQ-codegen configuration text replacement.

Default:

|Key                |Value (example)                                |
|-------------------|-----------------------------------------------|
|TARGET_DIRECTORY   |/path/to/target/scala-2.13/src_managed/main    |
|RESOURCE_DIRECTORY |/path/to/src/main/jooq-codegen/resources       |

```scala mdoc:compile-only
jooqCodegenVariables ++= Map(
  "JDBC_DRIVER" -> "org.h2.Driver",
  "JDBC_URL" -> "jdbc:h2:path/to/database",
)
```

The plugin replaces placeholders like `${KEY}` in configuration to variables.

e.g. Configuration file contains some placeholders:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-@JOOQ_MINOR_VERSION@.0.xsd">
    <jdbc>
        <driver>${JDBC_DRIVER}</driver>
        <url>${JDBC_URL}</url>
    </jdbc>
    <generator>
        <!-- ...snip... -->
        <target>
            <packageName>com.example</packageName>
            <directory>${TARGET_DIRECTORY}</directory>
        </target>
    </generator>
</configuration>
```

Then replace to:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-@JOOQ_MINOR_VERSION@.0.xsd">
    <jdbc>
        <driver>org.h2.Driver</driver>
        <url>jdbc:h2:path/to/database</url>
    </jdbc>
    <generator>
        <!-- ...snip... -->
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
