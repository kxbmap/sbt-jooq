sbt-jooq
========

jOOQ plugin for SBT 0.13.5+

Usage
-----
Add the following to your `project/plugins.sbt`:

* Stable version

  Not yet released.

* Snapshot version

  ```scala
  addSbtPlugin("com.github.kxbmap" % "sbt-jooq" % "0.1.0-SNAPSHOT")
  resolvers += Opts.resolver.sonatypeSnapshots
  ```

In your `build.sbt`:

* Enable the plugin:

  ```scala
  enablePlugins(JooqCodegen)
  ```

* Setting the jOOQ codegen configuration file:

  ```scala
  jooqCodegenConfigFile := file("path") / "to" / "jooq-codegen.xml"
  ```
  
* Add your database driver dependency to `jooq` scope:

  ```scala
  libraryDependencies += "com.h2database" % "h2" % "1.4.187" % "jooq"
  ```

License
-------
Apache License, Version 2.0
