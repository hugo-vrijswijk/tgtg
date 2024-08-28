package tgtg

import cats.Show
import cats.data.Chain
import cats.syntax.all.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

extension (duration: FiniteDuration)
  final def toHumanReadable: String =
    val units = Seq(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS)

    val secondsRounded = (duration.toMillis.toFloat / 1000).round.toLong
    val timeStrings = units
      .foldLeft((Chain.empty[String], secondsRounded)):
        case ((humanReadable, rest), unit) =>
          val name   = unit.toString().toLowerCase()
          val result = unit.convert(rest, TimeUnit.SECONDS)
          val diff   = rest - TimeUnit.SECONDS.convert(result, unit)
          val str = result match
            case 0    => humanReadable
            case 1    => humanReadable :+ show"1 ${name.init}" // Drop last 's'
            case more => humanReadable :+ show"$more $name"
          (str, diff)
      ._1

    timeStrings match
      case Chain()  => "0 seconds"
      case Chain(a) => a
      case _ =>
        val (strings, last) = timeStrings.initLast.get
        strings.mkString_(", ") + " and " + last
    end match
