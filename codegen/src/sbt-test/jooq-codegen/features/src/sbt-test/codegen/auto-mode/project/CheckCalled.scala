import java.util.concurrent.atomic.AtomicInteger
import sbt._
import sbt.Keys._
import sbtjooq.codegen.JooqCodegenKeys._
import sbtjooq.codegen.JooqCodegenPlugin

object CheckCalled extends AutoPlugin {

  override def requires: Plugins = JooqCodegenPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val checkCalled = inputKey[Unit]("")
  }
  import autoImport._

  private val counter = new AtomicInteger(0)

  override def projectSettings: Seq[Setting[_]] = Seq(
    JooqCodegen / run := {
      counter.incrementAndGet()
      (JooqCodegen / run).evaluated
    },
    checkCalled := {
      import sbt.complete.DefaultParsers._
      val n = (Space ~> IntBasic).parsed
      val c = counter.getAndSet(0)
      require(c == n, s"Required to called $n times, but called $c times")
    },
  )

}
