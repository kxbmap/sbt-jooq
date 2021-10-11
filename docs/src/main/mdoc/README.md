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

___
### Installation

Add the following to `project/plugins.sbt`:

```scala mdoc:compile-only
addSbtPlugin("com.github.kxbmap" % "sbt-jooq-codegen" % "@VERSION@")
```

Then in `build.sbt`:

```scala mdoc:compile-only
// Enable the plugin
enablePlugins(JooqCodegenPlugin)

// Add a driver dependency for the database you are using to "jooq-codegen" scope
libraryDependencies += "com.h2database" % "h2" % "@H2_VERSION@" % JooqCodegen
```

___
### Tasks

#### jooqCodegen

Run jOOQ-codegen manually according to settings.

#### jooqCodegenIfAbsent

Run jOOQ-codegen manually according to settings if generated files absent.

If generated files present, there is no effect.

___
### Settings

#### jooqVersion

- Optional (but recommended)
- Default: `"@JOOQ_VERSION@"`

Version of the jOOQ libraries to be used.

```scala mdoc:compile-only
jooqVersion := "@JOOQ_VERSION@"
```

#### jooqOrganization

- Optional
- Default: `"org.jooq"`

Organization/GroupID of jOOQ libraries.

If you want to use the commercial version of jOOQ, set the appropriate one. For details, please refer to
the [jOOQ manual](https://www.jooq.org/doc/@JOOQ_MINOR_VERSION@/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step1/).

```scala mdoc:compile-only
jooqOrganization := "org.jooq.trial"
```

#### autoJooqLibrary

- Optional
- Default: `true`

Add jOOQ dependencies automatically if true.

If you want to manage jOOQ dependencies yourself, set this flag to false.

```scala mdoc:compile-only
autoJooqLibrary := true
```

#### jooqCodegenMode

- Optional
- Default: `CodegenMode.Auto`

Mode of jOOQ-codegen execution.

|Mode         |Before each compile            |Directory where generated files placed         |
|-------------|-------------------------------|-----------------------------------------------|
|`Auto`       |Run `jooqCodegenIfAbsent` task |target/scala-<SCALA_VERSION>/src_managed/main/ |
|`Always`     |Run `jooqCodegen` task         |target/scala-<SCALA_VERSION>/src_managed/main/ |
|`Unmanaged`  |Do nothing                     |src/main/jooq-generated/                       |

`Auto` and `Always` are "Managed" mode that runs generation task before each compile and places
generated files to managed directory.

`Unmanaged` mode do nothing before compile. You should run generation task manually.
And generated files are placed in unmanaged directory.

```scala mdoc:compile-only
jooqCodegenMode := CodegenMode.Unmanaged
```

#### jooqCodegenConfig

- Required
- Default: `empty`

Configuration of jOOQ-codegen.

This is empty by default and must be set. You can set a file, classpath resource, XML directly, or multiple of them.


```scala mdoc:compile-only
// Set a file
// Note: Relative path is resolved from the root of the project.
jooqCodegenConfig := file("path/to/jooq-codegen.xml")
```

```scala mdoc:compile-only
// Set a URI of classpath resource
// Note: Resources are loaded from the `jooq-codegen` scope.
//       (e.g. resources under the `./src/jooq-codegen/resources/` directory)
jooqCodegenConfig := uri("classpath:path/to/jooq-codegen.xml")
```

```scala mdoc:compile-only
// Set XML directly using XML literal
jooqCodegenConfig :=
  <configuration>
    <!-- Your configurations -->
  </configuration>
```

```scala mdoc:compile-only
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
- Default:

  |KEY                |VALUE                                          |
  |-------------------|-----------------------------------------------|
  |RESOURCE_DIRECTORY |/path/to/src/main/jooq-codegen/resources       |

Variables for configuration placeholders replacement.

```scala mdoc:compile-only
jooqCodegenVariables ++= Map(
  "RESOURCE_DIRECTORY" -> (JooqCodegen / resourceDirectory).value
)
```

___
### Rewriting configurations

The plugin replaces placeholders like `${KEY}` in configurations to variables value.
If value is `java.util.Properties`, it expands to property elements that contains key and value elements,
otherwise to string.

And inserts a generator target directory element if it is not configured.

For example, add variables in build:

```scala mdoc:compile-only
jooqCodegenVariables ++= Map(
  "JDBC_DRIVER" -> "org.h2.Driver",
  "JDBC_URL" -> "jdbc:h2:path/to/database",
  "JDBC_PROPS" -> {
    val props = new java.util.Properties()
    props.setProperty("user", "root")
    props.setProperty("password", "secret")
    props
  },
)
```

and configuration is below:

```scala mdoc:invisible
import sbtjooq.codegen.internal._

val xml = 
  <configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-@JOOQ_MINOR_VERSION@.0.xsd">
      <jdbc>
          <driver>${{JDBC_DRIVER}}</driver>
          <url>${{JDBC_URL}}</url>
          <properties>${{JDBC_PROPS}}</properties>
      </jdbc>
      <generator>
          <target>
              <packageName>com.example</packageName>
          </target>
      </generator>
  </configuration>

val target = file("/path/to/target/scala-2.13/src_managed/main")

val vars = Map(
  "JDBC_DRIVER" -> "org.h2.Driver",
  "JDBC_URL" -> "jdbc:h2:path/to/database",
  "JDBC_PROPS" -> {
    val props = new java.util.Properties()
    props.setProperty("user", "root")
    props.setProperty("password", "secret")
    props
  },
)

val transform =
  Codegen.configTransformer(target, vars, Codegen.expandVariable.applyOrElse(_, Codegen.expandVariableFallback))

val pp = new scala.xml.PrettyPrinter(120, 4, true)
```
````scala mdoc:passthrough
println(s"""```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
${pp.format(xml)}
```""")
````

The plugin rewrites them before code generation:

````scala mdoc:passthrough
println(s"""```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
${pp.format(transform(xml)).replace('\\', '/')}
```""")
````

## License

Copyright 2015-2018 Tsukasa Kitachi

Apache License, Version 2.0
