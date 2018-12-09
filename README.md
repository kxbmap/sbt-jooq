# sbt-jooq

[![Build Status](https://travis-ci.org/kxbmap/sbt-jooq.svg?branch=v0.4.x)](https://travis-ci.org/kxbmap/sbt-jooq)

jOOQ plugin for sbt 1.0+


## Installation

Add the following to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.kxbmap" % "sbt-jooq" % "0.4.0")
```

Then in your `build.sbt`:

```scala
// Enable the plugin
enablePlugins(JooqCodegen)

// Add your database driver dependency to `jooq` scope
libraryDependencies += "com.h2database" % "h2" % "1.4.197" % "jooq"
```

### Configuration

#### jooqVersion
Version of jOOQ library.

Default: `"3.11.5"`

```scala
jooqVersion := "3.11.5"
```

#### jooqOrganization
jOOQ organization/group ID.

If you want to use commercial version of jOOQ, set appropriate one.
For details, please refer to the [jOOQ manual](https://www.jooq.org/doc/3.11/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step1/).

Default: `"org.jooq"`

```scala
jooqOrganization := "org.jooq"
```

#### autoJooqLibrary
Add jOOQ dependencies automatically if true.

If you want to manage jOOQ dependencies manually, set this flag to false.

Default: `true`

```scala
autoJooqLibrary := true
```

#### jooqCodegenConfig
jOOQ-codegen configuration. Set file or classpath resource or XML directly.

```scala
// Set file path
jooqCodegenConfig := baseDirectory.value / "jooq-codegen.xml"
```

```scala
// Set classpath resource using String prefixed by `classpath:`
jooqCodegenConfig := "classpath:jooq-codegen.xml" 
```

```scala
// Set XML configuration
jooqCodegenConfig :=
  <configuration>
    <!-- Your configurations -->
  </configuration>
```

#### jooqCodegenStrategy
jOOQ-codegen auto execution strategy.

|Value      |Description                                              |
|-----------|---------------------------------------------------------|
|`IfAbsent` |Execute if absent files in jOOQ-codegen target directory |
|`Always`   |Always execute                                           |
|`Never`    |Never execute                                            |

Default: `CodegenStrategy.IfAbsent`

```scala
jooqCodegenStrategy := CodegenStrategy.IfAbsent
```

#### jooqCodegenKeys
Keys for jOOQ-codegen configuration text substitution.

For details, please refer the below section.

Default: `sys.env ++ Seq(baseDirectory, sourceManaged in Compile)`

```scala
jooqCodegenKeys ++= Seq[CodegenKey](
  scalaVersion,     // Setting key
  skip in publish,  // Task key
  "Answer" -> 42    // constant  
)
```

You can confirm substitution values using `jooqCodegenSubstitutions`.
```
> show jooqCodegenSubstitutions
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
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-3.11.0.xsd">
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
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-3.11.0.xsd">
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
