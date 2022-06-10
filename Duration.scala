import cats.Show
import cats.data.Chain
import cats.syntax.foldable.*
import cats.syntax.show.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.CommandLineParser.FromString.given_FromString_Int

extension (duration: FiniteDuration)
  final def toHumanReadable: String =
    val units = Seq(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)

    val timeStrings = units
      .foldLeft((Chain.empty[String], duration.toMillis)) { case ((humanReadable, rest), unit) =>
        val name   = unit.toString().toLowerCase()
        val result = unit.convert(rest, TimeUnit.MILLISECONDS)
        val diff   = rest - TimeUnit.MILLISECONDS.convert(result, unit)
        val str = result match
          case 0    => humanReadable
          case 1    => humanReadable :+ show"1 ${name.init}" // Drop last 's'
          case more => humanReadable :+ show"$more $name"
        (str, diff)
      }
      ._1

    timeStrings match
      case Chain()  => "0 seconds"
      case Chain(a) => a
      case _ =>
        val (strings, last) = timeStrings.initLast.get
        strings.mkString_(", ") + " and " + last
    end match

given Show[FiniteDuration] with
  def show(t: FiniteDuration): String = t.toHumanReadable
