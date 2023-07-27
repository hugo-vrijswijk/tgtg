[![Actions Status](https://github.com/hugo-vrijswijk/tgtg/workflows/CI/badge.svg)](https://github.com/hugo-vrijswijk/tgtg/actions)

# TooGoodToGo Notifier

Never miss a TooGoodToGo box again!

## Introduction

The TooGoodToGo Notifier is a simple application that will notify you when any store in your TooGoodToGo favorites has a box available. It checks your favorite stores and sends a notification to [Gotify](https://gotify.net/), [Pushbullet](https://www.pushbullet.com/), or [Pushover](https://pushover.net/) when a box is available.

### Key Features

- Get notified instantly when your favorite TooGoodToGo stores have available boxes.
- Flexible setup: run once or continuously in server mode with a configurable interval.
- Option to use Redis storing data, enabling stateless application runs.

## Getting Started

### Prerequisites

Before using the application, you will need your TooGoodToGo user ID and refresh token. If you don't have them, you can obtain them with the following command:

```bash
$ tgtg auth
```

For Docker users, you can run the command inside a Docker container:

```bash
$ docker run -it --rm ghcr.io/hugo-vrijswijk/tgtg:latest auth
```

The command will guide you through the process of obtaining your user ID and refresh token.

### Installation

You have two options for running the application:

1. **Download a binary from the latest release**: [Release Page](https://github.com/hugo-vrijswijk/tgtg/releases)
2. **Use the Docker image**:

```bash
$ docker run ghcr.io/hugo-vrijswijk/tgtg:latest
```

### Usage

The application can be run in two modes:

1. **One-shot mode**: The application will check for boxes once and then exit.
2. **Server mode**: The application will continuously check for boxes at a configurable interval (default: 5 minutes) and never exit.

To run the application, you need to provide your TooGoodToGo user ID and refresh token as environment variables (`TGTG_USER_ID` and `TGTG_REFRESH_TOKEN`) or as arguments (`--user-id` and `--refresh-token`).

For more information on available options, run the following command:

```bash
$ tgtg --help
```

#### Using Redis for application state

By default, `tgtg` stores authentication tokens and notification history cache in a local file named `cache.json`. However, if you want to have a stateless application, you can use Redis for storing this state instead.

To use Redis, provide the Redis host using the `--redis-host` option:

```bash
$ tgtg --redis-host <REDIS_HOST>
```

Alternatively, set the Redis host as an environment variable (`REDIS_HOST`).

## Notifications

You can choose one of the following options for receiving notifications of an available box:

- Gotify: Set `--gotify-url` and `--gotify-token`.
- Pushbullet: Set `--pushbullet-token`.
- Pushover: Set `--pushover-token` and `--pushover-user`.

Each of these options can also be configured as environment variables (in `SCREAMING_SNAKE_CASE`).

## Acknowledgments

This application was inspired by [tgtg-python](https://github.com/ahivert/tgtg-python). We would like to express our gratitude to the developers of tgtg-python for their work.

## Development

The TooGoodToGo Notifier was developed using the following technologies:

- [Scala CLI](https://scala-cli.virtuslab.org/) (and [GraalVM](https://www.graalvm.org/))
- [Cats Effect](https://typelevel.org/cats-effect/) (and [FS2](https://fs2.io/))
- [Decline](https://ben.kirw.in/decline/)
- [Sttp](https://sttp.softwaremill.com/en/stable/)
- [Woof](https://github.com/LEGO/woof)
- [Circe](https://circe.github.io/circe/)
- [Rediculous](https://davenverse.github.io/rediculous/)
