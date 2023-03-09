import sbt.*

object ProjectUtil {

  def partialVersionSeq[A](version: String)(pf: PartialFunction[(Long, Long), Seq[A]]): Seq[A] =
    CrossVersion.partialVersion(version)
      .flatMap(pf.lift)
      .getOrElse(Seq.empty)

}
