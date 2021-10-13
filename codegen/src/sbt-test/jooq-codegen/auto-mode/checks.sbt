JooqCodegen / run := {
  Counter.increment()
  (JooqCodegen / run).evaluated
}

InputKey[Unit]("checkCalled") := {
  import sbt.complete.DefaultParsers._
  val n = (Space ~> IntBasic).parsed
  val c = Counter.getAndReset()
  require(c == n, s"Required to called $n times, but called $c times")
}
