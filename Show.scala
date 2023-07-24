package tgtg

import cats.Show
import sttp.model.Uri

import scala.concurrent.duration.FiniteDuration

given Show[Uri] = Show.fromToString

given Show[FiniteDuration] = _.toHumanReadable

given Show[Throwable] = Show.fromToString
