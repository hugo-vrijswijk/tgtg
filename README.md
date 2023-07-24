[![Actions Status](https://github.com/hugo-vrijswijk/tgtg/workflows/CI/badge.svg)](https://github.com/hugo-vrijswijk/tgtg/actions)

# TooGoodToGo Notifier

Never miss a TooGoodToGo box again!

Simple application that will notify you when any stores in your TooGoodToGo favourites has a box available. It will check your favourite stores and send a notification to Gotify (more notification services coming soon) when a box is available.

Can be set up to run once (for example with a cronjob) or to run continuously (with a configurable interval).

## First time usage

You will need your TooGoodToGo user id and refresh token. You can get them with the `auth` command:

```bash
$ tgtg auth
```

Or for docker:

```bash
$ docker run -it --rm ghcr.io/hugo-vrijswijk/tgtg:main auth
```

You will be asked for your email, and once you have entered it, you will receive an email to register a new device. Once registered, your user id and refresh token will be printed to the console.

## Usage

Download a binary from the latest release, or use the docker image:

```bash
$ docker run ghcr.io/hugo-vrijswijk/tgtg:main
```

## Thanks

Inspired by [tgtg-python](https://github.com/ahivert/tgtg-python). This app would not be possible without it.
