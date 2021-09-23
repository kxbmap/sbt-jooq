import java.util.concurrent.atomic.AtomicInteger

object Counter {
  private val counter = new AtomicInteger(0)

  def getAndReset(): Int = {
    counter.getAndSet(0)
  }

  def increment(): Unit = {
    counter.incrementAndGet()
  }
}
