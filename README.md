sbt-jooq
========

jOOQ plugin for SBT 0.13.5+


Installation
------------

Add the following to your `project/plugins.sbt`:

* Stable version

  Not yet released.

* Snapshot version

  ```scala
  addSbtPlugin("com.github.kxbmap" % "sbt-jooq" % "0.1.0-SNAPSHOT")

  resolvers += Opts.resolver.sonatypeSnapshots
  ```

Configuration
-------------

In your `build.sbt`:

```scala
// Enable the plugin
enablePlugins(JooqCodegen)

// Add your database driver dependency to `jooq` scope
libraryDependencies += "com.h2database" % "h2" % "1.4.187" % "jooq"

// optional: jOOQ library version (default: 3.6.2)
jooqVersion := "3.6.2"

// required: configuration file
// NOTE: target directory will replace by jooqCodegenTargetDirectory
jooqCodegenConfigFile := Some(file("path") / "to" / "jooq-codegen.xml")

// optional: generator target directory (default: sourceManaged in Compile)
jooqCodegenTargetDirectory := file("path") / "to" / "target" / "directory"
```

License
-------

Apache License, Version 2.0
