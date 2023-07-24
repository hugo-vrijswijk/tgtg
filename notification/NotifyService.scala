package tgtg.notification

import cats.effect.IO
import sttp.model.Uri
import tgtg.GotifyToken

trait NotifyService:
  def sendNotification(notification: Notification): IO[Unit]

final case class Notification(title: String, message: String)

enum NotifyConfig:
  case Gotify(token: GotifyToken, url: Uri)
