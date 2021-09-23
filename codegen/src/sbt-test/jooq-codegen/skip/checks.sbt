Compile / jooqCodegen := {
  Counter.increment()
  (Compile / jooqCodegen).value
}

TaskKey[Unit]("checkCalledOnce") := {
  val c = Counter.getAndReset()
  require(c == 1, s"Required to called once but $c")
}

TaskKey[Unit]("checkCalledNever") := {
  val c = Counter.getAndReset()
  require(c == 0, s"Required to called never but $c")
}
