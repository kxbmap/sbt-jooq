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

### Configuration

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
jOOQ-codegen configuration. This is empty by default and must be set. You can set a file, classpath resource, or XML directly.

```scala mdoc:compile-only
// Set file path
jooqCodegenConfig := file("jooq-codegen.xml")
```

```scala mdoc:compile-only
// Set URI of classpath resource
jooqCodegenConfig := uri("classpath:jooq-codegen.xml")
```

```scala mdoc:compile-only
// Set XML configuration
jooqCodegenConfig :=
  <configuration>
    <!-- Your configurations -->
  </configuration>
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
Compile / jooqCodegenAutoStrategy := AutoStrategy.IfAbsent
```

#### jooqCodegenKeys
Keys for jOOQ-codegen configuration text substitution.

For details, please refer the below section.

Default: `sys.env ++ Seq(baseDirectory, sourceManaged in Compile)`

```scala mdoc:compile-only
Compile / jooqCodegenKeys ++= Seq[CodegenKey](
  scalaVersion,     // Setting key
  publish / skip,   // Task key
  "Answer" -> 42    // constant  
)
```

You can confirm substitution values using `jooqCodegenSubstitutions`.
```
> show Compile / jooqCodegenSubstitutions
* ...Env vars...
* (baseDirectory, /path/to/base-directory)
* (compile:sourceManaged, /path/to/source-managed)
* (sourceManaged, /path/to/source-managed)
* (scalaVersion, 2.12.6)
* (publish::skip, false)
* (Answer, 42)
```

### jOOQ-codegen configuration text substitution
You can substitute text using placeholder(`${KEY}`) in configuration file.

e.g. Configuration file contains some placeholders:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-@JOOQ_MINOR_VERSION@.0.xsd">
    <jdbc>
        <!-- ...snip... -->
        <user>${DB_USER}</user>
        <password>${DB_PASSWORD}</password>
    </jdbc>
    <generator>
        <!-- ...snip... -->
        <target>
            <packageName>com.example</packageName>
            <directory>${sourceManaged}</directory>
        </target>
    </generator>
</configuration>
```

Plugin replace placeholders to substitution values:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-@JOOQ_MINOR_VERSION@.0.xsd">
    <jdbc>
        <!-- ...snip... -->
        <user>your-user</user>
        <password>your-password</password>
    </jdbc>
    <generator>
        <!-- ...snip... -->
        <target>
            <packageName>com.example</packageName>
            <directory>/path/to/source-managed</directory>
        </target>
    </generator>
</configuration>
```

## License

Copyright 2015-2018 Tsukasa Kitachi

Apache License, Version 2.0
