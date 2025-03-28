# TooGoodToGo Notifier

Never Miss a TooGoodToGo Box Again!

[![Actions Status](https://github.com/hugo-vrijswijk/tgtg/workflows/CI/badge.svg)](https://github.com/hugo-vrijswijk/tgtg/actions)

## Introduction

The TooGoodToGo Notifier is a simple yet powerful application designed to keep you updated about available boxes from your favorite stores on TooGoodToGo. It automates the process of checking for boxes and sends notifications to your preferred platforms whenever a box becomes available.

### Key Features

- Instant notifications when your favorite TooGoodToGo stores have boxes available.
- Flexibility to run as a one-time check or continuously with customizable intervals or CRON schedules.
- Option to utilize Redis for data storage, enabling stateless application runs.

## Getting Started

Before using the application, you'll need your TooGoodToGo refresh token. If you don't have it yet, you can obtain it with this command:

```bash
$ tgtg auth
```

For Docker users, you can run the command within a Docker container:

```bash
$ docker run -it --rm ghcr.io/hugo-vrijswijk/tgtg:latest auth
```

The command will guide you through obtaining your refresh token.

### Installation

You have two options for running the application:

1. **Download a Binary from the Latest Release**: [Release Page](https://github.com/hugo-vrijswijk/tgtg/releases)
2. **Use the Docker Image**:

```bash
$ docker run ghcr.io/hugo-vrijswijk/tgtg:latest
```

## Usage

The application operates in two modes:

1. **One-shot Mode**: Checks for boxes once and then exits.
2. **Server Mode**: Continuously monitors for boxes at [configured schedules](#schedules) without exiting.

To run the application, provide your TooGoodToGo refresh token as environment variable (`TGTG_REFRESH_TOKEN`) or as argument (`--refresh-token`) and a [notification provider](#notifications).

For detailed information about available options, use the following command:

```bash
$ tgtg --help
```

### Notifications

Choose your preferred notification platform for available box alerts:

- Gotify: `--gotify-url` and `--gotify-token`.
- Pushbullet: `--pushbullet-token`.
- Pushover: `--pushover-token` and `--pushover-user`.
- Custom Webhook URL: `--webhook-url`
  - This will send a POST request to the URL with a JSON body containing a `title` and `message`.

Each of these options can also be set as environment variables (in `SCREAMING_SNAKE_CASE`).

### Schedules

To enable server mode, specify intervals or CRON schedules for checking TooGoodToGo boxes:

- Intervals: `--interval <duration>` (e.g., `--interval 5m` for every 5 minutes).
- CRON Schedules: `--cron <cron>` (e.g., `--cron "0 0 0 * * ?"` for every day at midnight).

`tgtg` will run immediately, and then at the specified intervals or CRON schedules.

Intervals and CRON schedules can be combined as many times as needed. For example:

```bash
$ tgtg --interval 5m --cron "0 0 0 * * ?" --cron "0 0 12 * * ?"
```

#### Examples

Here is an example of running the notifier in server mode with some advanced CRON expressions. Tokens are omitted:

```bash
$ tgtg --cron '0 */2 9-15,18-20 ? * *' --cron '0,30 * 16,17 ? * fri' --cron '0 */2 16,17 ? * mon-thu,sat,sun'
```

Start time is inclusive, and end time is exclusive. `9-15` will start at 09:00 and end at 15:59.

This has 3 CRON schedules:

1. `0 */2 9-15,18-20 ? * *`: Every 2 minutes between 09:00 and 15:59, and between 18:00 and 20:59. On every day.
2. `0,30 * 16,17 ? * fri`: Every 30 seconds between 16:00 and 17:59 on Fridays.
3. `0 */2 16,17 ? * mon-thu,sat,sun`: Every 2 minutes between 16:00 and 17:59 on other days.

This will run the TooGoodToGo Notifier more frequently during times a box I want is likely to be available, less frequently during other times, and not at all during the night.

### Utilizing Redis for Application State (advanced)

By default, `tgtg` stores authentication tokens and notification history cache in a local file named `cache.json`. For a stateless application, Redis can be used to manage this state.

To use Redis, specify the Redis host with the `--redis-host` option:

```bash
$ tgtg --redis-host <REDIS_HOST>
```

Alternatively, set the Redis host as an environment variable (`REDIS_HOST`).

## Development

The TooGoodToGo Notifier was developed using the following technologies:

- [Scala CLI](https://scala-cli.virtuslab.org/) (and [GraalVM](https://www.graalvm.org/))
- [Cats Effect](https://typelevel.org/cats-effect/) (and [FS2](https://fs2.io/))
- [Decline](https://ben.kirw.in/decline/)
- [Sttp](https://sttp.softwaremill.com/en/stable/)
- [Woof](https://github.com/LEGO/woof)
- [Circe](https://circe.github.io/circe/)
- [Rediculous](https://davenverse.github.io/rediculous/)

## Acknowledgments

This application drew inspiration from [tgtg-python](https://github.com/ahivert/tgtg-python). We express our gratitude to the developers of tgtg-python for their valuable contributions.
