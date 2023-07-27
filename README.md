[![Actions Status](https://github.com/hugo-vrijswijk/tgtg/workflows/CI/badge.svg)](https://github.com/hugo-vrijswijk/tgtg/actions)

# TooGoodToGo Notifier

Never miss a TooGoodToGo box again!

Simple application that will notify you when any store in your TooGoodToGo favourites has a box available. It will check your favourite stores and send a notification to [Gotify, Pushbullet or Pushover](#notifications) when a box is available.

It can be set up to run once (for example, with a cronjob) or to run continuously (with a configurable interval).

## First time usage

You will need your TooGoodToGo user ID and refresh token. You can get them with the `auth` command:

```bash
$ tgtg auth
```

Or for docker:

```bash
$ docker run -it --rm ghcr.io/hugo-vrijswijk/tgtg:latest auth
```

You will be asked for your email, and once you have entered it, you will receive an email to register a new device. Once registered, your user ID and refresh token will be printed to the console. Use these as environment variables (`TGTG_USER_ID` and `TGTG_REFRESH_TOKEN`) or arguments (`--user-id` and `--refresh-token`) when running the application.

## Usage

Download a binary from the latest release, or use the docker image:

```bash
$ docker run ghcr.io/hugo-vrijswijk/tgtg:latest
```

You can run tgtg once, or continuously (a.k.a. 'server mode'). In server mode, the application will check for boxes at a configurable interval (default: 5 minutes) and never exit. In one-shot mode, the application will check for boxes once and then exit.

## Notifications

There are currently 3 options for sending notifications of an available box:

- Gotify: with `--gotify-url` and `--gotify-token`
- Pushbullet: with `--pushbullet-token`
- Pushover: with `--pushover-token` and `--pushover-user`

Each of these options can also be an environment variable (in `SCREAMING_SNAKE_CASE`). Run `tgtg --help` for all options.

## Thanks

Inspired by [tgtg-python](https://github.com/ahivert/tgtg-python). This app wouldn't exist without it.

Developed using:

- [Scala CLI](https://scala-cli.virtuslab.org/) (and [GraalVM](https://www.graalvm.org/))
- [Cats Effect](https://typelevel.org/cats-effect/) (and [FS2](https://fs2.io/))
- [Decline](https://ben.kirw.in/decline/)
- [Sttp](https://sttp.softwaremill.com/en/stable/)
- [Woof](https://github.com/LEGO/woof)
- [Circe](https://circe.github.io/circe/)
- [Rediculous](https://davenverse.github.io/rediculous/)
