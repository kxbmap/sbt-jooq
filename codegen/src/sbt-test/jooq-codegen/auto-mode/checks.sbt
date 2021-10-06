JooqCodegen / run := {
  Counter.increment()
  (JooqCodegen / run).evaluated
}

TaskKey[Unit]("checkCalledOnce") := {
  val c = Counter.getAndReset()
  require(c == 1, s"Required to called once, but called $c times")
}

TaskKey[Unit]("checkCalledTwice") := {
  val c = Counter.getAndReset()
  require(c == 2, s"Required to called twice, but called $c times")
}
