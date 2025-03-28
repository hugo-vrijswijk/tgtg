# Changelog

## [1.0.5](https://github.com/hugo-vrijswijk/tgtg/compare/v1.0.5...v1.0.5) (2025-03-28)


### ⚠ BREAKING CHANGES

* The `--server` flag is deprecated. Use `--interval` or `--cron` options instead. When not used with `--interval` or `--cron`, it will use a default of 5 minutes.

### Features

* add ARM build ([#38](https://github.com/hugo-vrijswijk/tgtg/issues/38)) ([ce9b0aa](https://github.com/hugo-vrijswijk/tgtg/commit/ce9b0aacc89f8d47580d1ff6929ce14981d0be8c))
* add support for CRON schedules ([#85](https://github.com/hugo-vrijswijk/tgtg/issues/85)) ([522275f](https://github.com/hugo-vrijswijk/tgtg/commit/522275f9e713ebc05a7f239b0c2c481ca7873dc8))
* allow combining multiple intervals and CRON schedules ([522275f](https://github.com/hugo-vrijswijk/tgtg/commit/522275f9e713ebc05a7f239b0c2c481ca7873dc8))
* build macos aarch64 ([#77](https://github.com/hugo-vrijswijk/tgtg/issues/77)) ([888fd44](https://github.com/hugo-vrijswijk/tgtg/commit/888fd4411719a01594021523bf5c9656a9c2796d))
* **notifications:** add Pushbullet support ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* **notifications:** add Pushover support ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* **notifications:** add webhook notification provider ([#35](https://github.com/hugo-vrijswijk/tgtg/issues/35)) ([7c4e0c6](https://github.com/hugo-vrijswijk/tgtg/commit/7c4e0c61c35f2cb776d8e9aab306779b67cfc92d))
* setup release-please ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* show price in correct currency and decimals ([#63](https://github.com/hugo-vrijswijk/tgtg/issues/63)) ([b066fc2](https://github.com/hugo-vrijswijk/tgtg/commit/b066fc2531c78df8d54aff08c52b79adbbed15b6))


### Bug Fixes

* add 'Accept' to headers ([#30](https://github.com/hugo-vrijswijk/tgtg/issues/30)) ([c58947b](https://github.com/hugo-vrijswijk/tgtg/commit/c58947b4d63aad34fa35cdf7b6d0a59c58c53405))
* add Access-Token to sensitive headers config ([#93](https://github.com/hugo-vrijswijk/tgtg/issues/93)) ([45dc1a0](https://github.com/hugo-vrijswijk/tgtg/commit/45dc1a00ba8f96924bc6af27f4cca513adafd1fb))
* arm release ([#56](https://github.com/hugo-vrijswijk/tgtg/issues/56)) ([1ee7b71](https://github.com/hugo-vrijswijk/tgtg/commit/1ee7b711f98f5950e4dcf8646781544aa0d9cf07))
* dependency update ([#52](https://github.com/hugo-vrijswijk/tgtg/issues/52)) ([1635a3e](https://github.com/hugo-vrijswijk/tgtg/commit/1635a3e7c6adc7346be244749454b7284772c01b))
* **deps:** update fs2-cron to 0.10.1 ([#122](https://github.com/hugo-vrijswijk/tgtg/issues/122)) ([927e0d1](https://github.com/hugo-vrijswijk/tgtg/commit/927e0d140f089a05ac9f2de1e1f7c08b33760b41))
* **docker:** separate digests artifact downloads ([#68](https://github.com/hugo-vrijswijk/tgtg/issues/68)) ([9c9edd7](https://github.com/hugo-vrijswijk/tgtg/commit/9c9edd72d4756258d522f1079f0c1be0a42ea011))
* **docker:** use separate jobs to build arm architectures ([#66](https://github.com/hugo-vrijswijk/tgtg/issues/66)) ([309513d](https://github.com/hugo-vrijswijk/tgtg/commit/309513d8f6d3dfa85105d494b3243829f8ac09ed))
* fix docker release tagging ([#26](https://github.com/hugo-vrijswijk/tgtg/issues/26)) ([1b496ea](https://github.com/hugo-vrijswijk/tgtg/commit/1b496ea47362e9870908d53a165fa9a839d44e96))
* fix windows artifact build ([#23](https://github.com/hugo-vrijswijk/tgtg/issues/23)) ([79c9ec4](https://github.com/hugo-vrijswijk/tgtg/commit/79c9ec452d2068a8c89b799eb5f1fe4fb1956d02))
* log correct usage of --user-email on error ([#29](https://github.com/hugo-vrijswijk/tgtg/issues/29)) ([882d59b](https://github.com/hugo-vrijswijk/tgtg/commit/882d59b19951e4331d0bb1c2bb932be9151c3a81))
* move some logging to debug ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* release jdk versions ([#54](https://github.com/hugo-vrijswijk/tgtg/issues/54)) ([80dc102](https://github.com/hugo-vrijswijk/tgtg/commit/80dc1025d7b1e2cc22ba38fcc9a29d9962806081))
* release-please version.txt ([#125](https://github.com/hugo-vrijswijk/tgtg/issues/125)) ([71dc9be](https://github.com/hugo-vrijswijk/tgtg/commit/71dc9beb957d476c5c20917219d1eeb863c98f06))
* remove release-type from release-please GHA config ([#128](https://github.com/hugo-vrijswijk/tgtg/issues/128)) ([798e1f2](https://github.com/hugo-vrijswijk/tgtg/commit/798e1f2eb56b4cbae2b97f70df3967c615d09f11))
* update token refresh endpoint ([#118](https://github.com/hugo-vrijswijk/tgtg/issues/118)) ([f611382](https://github.com/hugo-vrijswijk/tgtg/commit/f6113820591f010167c6d8d6fc69ae6538822cf5))
* use new auth endpoint version and user agents ([#100](https://github.com/hugo-vrijswijk/tgtg/issues/100)) ([5ecf02d](https://github.com/hugo-vrijswijk/tgtg/commit/5ecf02d38d7cf5211af06b96b8f2e8e97a510f06))
* use new tgtg auth mechanism ([#92](https://github.com/hugo-vrijswijk/tgtg/issues/92)) ([79b4eb1](https://github.com/hugo-vrijswijk/tgtg/commit/79b4eb1c298f885780b9ef27a793bb6a6d431997))
* **windows:** fix auth input error ([#48](https://github.com/hugo-vrijswijk/tgtg/issues/48)) ([57b00f2](https://github.com/hugo-vrijswijk/tgtg/commit/57b00f209a7228e2e69807d331bb4cb7f0facb9b))


### Miscellaneous Chores

* release 1.0.5 ([d352c42](https://github.com/hugo-vrijswijk/tgtg/commit/d352c4212e1bda0172bed334c30385190a7082ce))


### Build System

* update version ([9288924](https://github.com/hugo-vrijswijk/tgtg/commit/92889249e29d3143a487eb6518d3935582e63d33))

## [1.0.5](https://github.com/hugo-vrijswijk/tgtg/compare/tgtg-v1.0.4...tgtg-v1.0.5) (2025-03-28)


### Bug Fixes

* Automatically update version.txt ([#125](https://github.com/hugo-vrijswijk/tgtg/issues/125)) ([71dc9be](https://github.com/hugo-vrijswijk/tgtg/commit/71dc9beb957d476c5c20917219d1eeb863c98f06))

### Build System

* update version ([9288924](https://github.com/hugo-vrijswijk/tgtg/commit/92889249e29d3143a487eb6518d3935582e63d33))

## [1.0.4](https://github.com/hugo-vrijswijk/tgtg/compare/v1.0.3...v1.0.4) (2025-03-27)


### Bug Fixes

* **deps:** update fs2-cron to 0.10.1 ([#122](https://github.com/hugo-vrijswijk/tgtg/issues/122)) ([927e0d1](https://github.com/hugo-vrijswijk/tgtg/commit/927e0d140f089a05ac9f2de1e1f7c08b33760b41))

## [1.0.3](https://github.com/hugo-vrijswijk/tgtg/compare/v1.0.2...v1.0.3) (2025-03-25)


### Bug Fixes

* update token refresh endpoint ([#118](https://github.com/hugo-vrijswijk/tgtg/issues/118)) ([f611382](https://github.com/hugo-vrijswijk/tgtg/commit/f6113820591f010167c6d8d6fc69ae6538822cf5))

## [1.0.2](https://github.com/hugo-vrijswijk/tgtg/compare/v1.0.1...v1.0.2) (2025-01-07)


### Bug Fixes

* use new auth endpoint version and user agents ([#100](https://github.com/hugo-vrijswijk/tgtg/issues/100)) ([5ecf02d](https://github.com/hugo-vrijswijk/tgtg/commit/5ecf02d38d7cf5211af06b96b8f2e8e97a510f06))

## [1.0.1](https://github.com/hugo-vrijswijk/tgtg/compare/v1.0.0...v1.0.1) (2024-11-25)


### Bug Fixes

* add Access-Token to sensitive headers config ([#93](https://github.com/hugo-vrijswijk/tgtg/issues/93)) ([45dc1a0](https://github.com/hugo-vrijswijk/tgtg/commit/45dc1a00ba8f96924bc6af27f4cca513adafd1fb))
* use new tgtg auth mechanism ([#92](https://github.com/hugo-vrijswijk/tgtg/issues/92)) ([79b4eb1](https://github.com/hugo-vrijswijk/tgtg/commit/79b4eb1c298f885780b9ef27a793bb6a6d431997))

## [1.0.0](https://github.com/hugo-vrijswijk/tgtg/compare/v0.7.0...v1.0.0) (2024-08-28)

This release adds support for CRON schedules. CRON schedules (`--cron`) and intervals (`--interval`) can be combined multiple times for complex scheduling:

```bash
$ tgtg --interval 5m --cron "0 0 0 * * ?" --cron "0 0 12 * * ?"
```

### ⚠ BREAKING CHANGES

* The `--server` flag is deprecated. Use `--interval` or `--cron` options instead. When not used with `--interval` or `--cron`, it will use a default of 5 minutes.

### Features

* add support for CRON schedules ([#85](https://github.com/hugo-vrijswijk/tgtg/issues/85)) ([522275f](https://github.com/hugo-vrijswijk/tgtg/commit/522275f9e713ebc05a7f239b0c2c481ca7873dc8))
* allow combining multiple intervals and CRON schedules ([#85](https://github.com/hugo-vrijswijk/tgtg/issues/85)) ([522275f](https://github.com/hugo-vrijswijk/tgtg/commit/522275f9e713ebc05a7f239b0c2c481ca7873dc8))

## [0.7.0](https://github.com/hugo-vrijswijk/tgtg/compare/v0.6.2...v0.7.0) (2024-04-30)


### Features

* build macos aarch64 ([#77](https://github.com/hugo-vrijswijk/tgtg/issues/77)) ([888fd44](https://github.com/hugo-vrijswijk/tgtg/commit/888fd4411719a01594021523bf5c9656a9c2796d))

## [0.6.2](https://github.com/hugo-vrijswijk/tgtg/compare/v0.6.1...v0.6.2) (2024-02-16)


### Bug Fixes

* **docker:** separate digests artifact downloads ([#68](https://github.com/hugo-vrijswijk/tgtg/issues/68)) ([9c9edd7](https://github.com/hugo-vrijswijk/tgtg/commit/9c9edd72d4756258d522f1079f0c1be0a42ea011))

## [0.6.1](https://github.com/hugo-vrijswijk/tgtg/compare/v0.6.0...v0.6.1) (2024-02-16)


### Bug Fixes

* **docker:** use separate jobs to build arm architectures ([#66](https://github.com/hugo-vrijswijk/tgtg/issues/66)) ([309513d](https://github.com/hugo-vrijswijk/tgtg/commit/309513d8f6d3dfa85105d494b3243829f8ac09ed))

## [0.6.0](https://github.com/hugo-vrijswijk/tgtg/compare/v0.5.4...v0.6.0) (2024-01-27)


### Features

* show price in correct currency and decimals ([#63](https://github.com/hugo-vrijswijk/tgtg/issues/63)) ([b066fc2](https://github.com/hugo-vrijswijk/tgtg/commit/b066fc2531c78df8d54aff08c52b79adbbed15b6))

## [0.5.4](https://github.com/hugo-vrijswijk/tgtg/compare/v0.5.3...v0.5.4) (2023-12-05)


### Bug Fixes

* arm release ([#56](https://github.com/hugo-vrijswijk/tgtg/issues/56)) ([1ee7b71](https://github.com/hugo-vrijswijk/tgtg/commit/1ee7b711f98f5950e4dcf8646781544aa0d9cf07))

## [0.5.3](https://github.com/hugo-vrijswijk/tgtg/compare/v0.5.2...v0.5.3) (2023-12-05)


### Bug Fixes

* release jdk versions ([#54](https://github.com/hugo-vrijswijk/tgtg/issues/54)) ([80dc102](https://github.com/hugo-vrijswijk/tgtg/commit/80dc1025d7b1e2cc22ba38fcc9a29d9962806081))

## [0.5.2](https://github.com/hugo-vrijswijk/tgtg/compare/v0.5.1...v0.5.2) (2023-12-05)


### Bug Fixes

* dependency update ([#52](https://github.com/hugo-vrijswijk/tgtg/issues/52)) ([1635a3e](https://github.com/hugo-vrijswijk/tgtg/commit/1635a3e7c6adc7346be244749454b7284772c01b))

## [0.5.1](https://github.com/hugo-vrijswijk/tgtg/compare/v0.5.0...v0.5.1) (2023-09-19)


### Bug Fixes

* **windows:** fix auth input error ([#48](https://github.com/hugo-vrijswijk/tgtg/issues/48)) ([57b00f2](https://github.com/hugo-vrijswijk/tgtg/commit/57b00f209a7228e2e69807d331bb4cb7f0facb9b))

## [0.5.0](https://github.com/hugo-vrijswijk/tgtg/compare/v0.4.0...v0.5.0) (2023-08-17)


### Features

* add ARM build ([#38](https://github.com/hugo-vrijswijk/tgtg/issues/38)) ([ce9b0aa](https://github.com/hugo-vrijswijk/tgtg/commit/ce9b0aacc89f8d47580d1ff6929ce14981d0be8c))

## [0.4.0](https://github.com/hugo-vrijswijk/tgtg/compare/v0.3.2...v0.4.0) (2023-08-15)


### Features

* **notifications:** add webhook notification provider ([#35](https://github.com/hugo-vrijswijk/tgtg/issues/35)) ([7c4e0c6](https://github.com/hugo-vrijswijk/tgtg/commit/7c4e0c61c35f2cb776d8e9aab306779b67cfc92d))

## [0.3.2](https://github.com/hugo-vrijswijk/tgtg/compare/v0.3.1...v0.3.2) (2023-07-28)


### Bug Fixes

* add 'Accept' to headers ([#30](https://github.com/hugo-vrijswijk/tgtg/issues/30)) ([c58947b](https://github.com/hugo-vrijswijk/tgtg/commit/c58947b4d63aad34fa35cdf7b6d0a59c58c53405))
* log correct usage of --user-email on error ([#29](https://github.com/hugo-vrijswijk/tgtg/issues/29)) ([882d59b](https://github.com/hugo-vrijswijk/tgtg/commit/882d59b19951e4331d0bb1c2bb932be9151c3a81))

## [0.3.1](https://github.com/hugo-vrijswijk/tgtg/compare/v0.3.0...v0.3.1) (2023-07-27)


### Bug Fixes

* fix docker release tagging ([#26](https://github.com/hugo-vrijswijk/tgtg/issues/26)) ([1b496ea](https://github.com/hugo-vrijswijk/tgtg/commit/1b496ea47362e9870908d53a165fa9a839d44e96))

## [0.3.0](https://github.com/hugo-vrijswijk/tgtg/compare/v0.3.0...v0.3.0) (2023-07-27)


### Features

* **notifications:** add Pushbullet support ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* **notifications:** add Pushover support ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* setup release-please ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))


### Bug Fixes

* fix windows artifact build ([#23](https://github.com/hugo-vrijswijk/tgtg/issues/23)) ([79c9ec4](https://github.com/hugo-vrijswijk/tgtg/commit/79c9ec452d2068a8c89b799eb5f1fe4fb1956d02))
* move some logging to debug ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))


### Build System

* update version ([9288924](https://github.com/hugo-vrijswijk/tgtg/commit/92889249e29d3143a487eb6518d3935582e63d33))

## [0.3.0](https://github.com/hugo-vrijswijk/tgtg/compare/v0.3.0...v0.3.0) (2023-07-27)


### Features

* **notifications:** add Pushbullet support ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* **notifications:** add Pushover support ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))
* setup release-please ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))


### Bug Fixes

* fix windows artifact build ([#23](https://github.com/hugo-vrijswijk/tgtg/issues/23)) ([79c9ec4](https://github.com/hugo-vrijswijk/tgtg/commit/79c9ec452d2068a8c89b799eb5f1fe4fb1956d02))
* move some logging to debug ([15b5826](https://github.com/hugo-vrijswijk/tgtg/commit/15b58264b2404b207a949a797f9e37fa1537d675))


### Build System

* update version ([9288924](https://github.com/hugo-vrijswijk/tgtg/commit/92889249e29d3143a487eb6518d3935582e63d33))
