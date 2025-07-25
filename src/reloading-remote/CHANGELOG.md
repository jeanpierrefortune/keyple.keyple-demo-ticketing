# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [2025.07.23]
### Added
- Added the `GET` REST resource `/export-card-selection-scenario` to allow the client to retrieve an exported card
  selection scenario.
- Added server-side management of the `initialCardContent` JSON property, to enable import of the processed card 
  selection scenario.
- Added a Kotlin Multiplatform client for Android and iOS platforms into `client/interop-mobile-multiplatform` directory.
- Added support for storage cards in addition to Calypso cards (only for Android client application).
### Changed
- Rename directory `client/android` to `client/keyple-mobile-android`
### Removed
- Removed the Android Light client

## [2025.05.23]
### Fixed
- SAM presence observation following PC/SC plugin upgrade.
### Upgraded
- Keyple components
  - keyple-plugin-pcsc-java-lib `2.4.2` -> `2.5.1`

## [2025.03.21]
### Upgraded
- Keypop API
  - keypop-calypso-card-java-api `2.1.0` -> `2.1.2`
  - keypop-calypso-crypto-legacysam-java-api `0.6.0` -> `0.7.0`
- Keyple components
  - keyple-service-java-lib `3.3.1` -> `3.3.5`
  - keyple-card-calypso-java-lib `3.1.3` -> `3.1.7`
  - keyple-card-calypso-crypto-legacysam-java-lib `0.8.0` -> `0.9.0`
  - keyple-plugin-android-nfc-java-lib `2.2.0` -> `3.0.0`
  - keyple-plugin-pcsc-java-lib `2.4.0` -> `2.4.2`
  - keyple-distributed-local-java-lib `2.5.1` -> `2.5.2`

## [2024.10.21]
### Added
- Added a new Android client application to demonstrate the use of the **Keyple Distributed JSON API** and without the
  need of the `keyple-card-calypso-java-lib` on terminal side. The corresponding code is located in the folder named
  `android-light`.
- Added new remote services in the server app to enhance keyple-less clients capabilities.
### Changed
- Rename repository from `keyple-java-demo-remote` to `keyple-demo-ticketing-reloading-remote`
- Rename Android artifact from `keyple-demo-remote-client-android` to `keyple-demo-ticketing-reloading-client-android-app`
- Rename Server artifact from `keyple-demo-remote-server` to `keyple-demo-ticketing-reloading-server`
- Change `minSdk` from `24` -> `26`
### Upgraded
- keyple-demo-common-lib `2.0.1-SNAPSHOT` -> keyple-demo-ticketing-common-lib `2.0.2-SNAPSHOT`
- Keyple components
  - keyple-service-java-lib `3.3.1` -> `3.3.3`
  - keyple-card-calypso-java-lib `3.1.3` -> `3.1.4`
  
## [2024.10.01]
### Upgraded
- Keyple components
  - keyple-service-java-lib `3.3.1` -> `3.3.3`
  - keyple-card-calypso-java-lib `3.1.3` -> `3.1.4`
  - keyple-plugin-pcsc-java-lib `2.2.3` -> `2.3.1`

## [2024.09.20]
### Fixed
- Android client build.
### Upgraded
- Keyple components
  - keyple-service-java-lib `3.2.1` -> `3.3.1`
  - keyple-service-resource-java-lib `3.0.1` -> `3.1.0`
  - keyple-distributed-network-java-lib `2.3.1` -> `2.5.1`
  - keyple-distributed-remote-java-lib `2.3.1` -> `2.5.1`
  - keyple-distributed-local-java-lib `2.3.1` -> `2.5.1`
  - keyple-card-calypso-java-lib `3.1.1` -> `3.1.3`
  - keyple-card-calypso-crypto-legacysam-java-lib `0.7.0` -> `0.8.0`
  - keyple-plugin-pcsc-java-lib `2.2.1` -> `2.2.3`
- C# application to comply with the **Keyple Distributed JSON API** 2.1

## [2024.04.23]
### Upgraded
- keyple-demo-common-lib `2.0.0-SNAPSHOT` -> `2.0.1-SNAPSHOT`
- All Keyple components (compiled to java 8)
    - keypop-reader-java-api `2.0.0` -> `2.0.1`
    - keypop-calypso-card-java-api `2.0.0` -> `2.1.0`
    - keypop-calypso-crypto-legacysam-java-api `0.3.0` -> `0.6.0`
    - keyple-common-java-api `2.0.0` -> `2.0.1`
    - keyple-util-java-lib `2.3.1` -> `2.4.0`
    - keyple-service-java-lib `3.0.1` -> `3.2.1`
    - keyple-distributed-network-java-lib `2.3.0` -> `2.3.1`
    - keyple-distributed-local-java-lib `2.3.0` -> `2.3.1`
    - keyple-distributed-local-java-lib `2.3.0` -> `2.3.1`
    - keyple-card-calypso-java-lib `3.0.1` -> `3.1.1`
    - keyple-card-calypso-crypto-legacysam-java-lib `0.4.0` -> `0.7.0`
    - keyple-plugin-android-nfc-java-lib `2.0.1` -> `2.2.0`
    - keyple-plugin-android-omapi-java-lib `2.0.1` -> `2.1.0`
    - keyple-plugin-pcsc-java-lib `2.1.2` -> `2.2.1`
- Other components (Gradle wrapper, Android Gradle Plugin, etc.)

## [2023.12.06]
### Upgraded
- Calypsonet Terminal Reader API `1.3.0` -> Keypop Reader API `2.0.0`
- Calypsonet Terminal Calypso API `1.8.0` -> Keypop Calypso Card API `2.0.0`
- Keyple Service Library `2.3.1` -> `3.0.1`
- Keyple Service Resource Library `2.1.1` -> `3.0.0`
- Keyple Calypso Card Library `2.3.5` -> `3.0.1`
- Keyple Util Library `2.3.0` -> `2.3.1`
- Keyple Distributed Local Library `2.2.0` -> `2.3.0`
- Keyple Distributed Network Library `2.2.0` -> `2.3.0`
- Keyple Distributed Remote Library `2.2.1` -> `2.3.0`

### Added
New dependencies
- Keypop Crypto Legacy SAM API `0.3.0`
- Keyple Calypso Crypto LegacySAM Library `0.4.0`

## [2023.05.31]
### Added
- Added a new C# application to demonstrate the use of the **Keyple Distributed JSON API** inside the `client/dotnet` folder.
### Upgraded
- `keyple-demo-common-lib:2.0.0-SNAPSHOT`
- `calypsonet-terminal-reader-java-api:1.3.0`
- `calypsonet-terminal-calypso-java-api:1.8.0`
- `keyple-service-java-lib:2.3.1`
- `keyple-service-resource-java-lib:2.1.1`
- `keyple-distributed-network-java-lib:2.2.0`
- `keyple-distributed-remote-java-lib:2.2.1`
- `keyple-card-calypso-java-lib:2.3.5`
- `keyple-plugin-pcsc-java-lib:2.1.2`

## [2023.03.03]
### Fixed
- Physical channel management.
- Missing Keyple generic AID.
- Calypso Basic products management.

## [2023.02.24]
### Upgraded
- `calypsonet-terminal-reader-java-api:1.2.0`
- `calypsonet-terminal-calypso-java-api:1.6.0`
- `keyple-service-java-lib:2.1.3`
- `keyple-card-calypso-java-lib:2.3.2`
- `keyple-distributed-remote-java-lib:2.1.0`
- `com.google.code.gson:gson:2.10.1`

## [2022.11.18]
### Fixed
- Various erroneous behaviors and displays.
### Added
- CI: `java-test` GitHub action.
### Changed
- Major refactoring of the source code.
### Upgraded
- `keyple-demo-common-lib:1.0.0-SNAPSHOT`
- `calypsonet-terminal-reader-java-api:1.1.0`
- `calypsonet-terminal-calypso-java-api:1.4.1`
- `keyple-service-java-lib:2.1.1`
- `keyple-service-resource-java-lib:2.0.2`
- `keyple-card-calypso-java-lib:2.2.5`
- `keyple-plugin-android-nfc-java-lib:2.0.1`
- `keyple-plugin-android-omapi-java-lib:2.0.1`
- `keyple-util-java-lib:2.3.0`

[Unreleased]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2025.07.23...HEAD
[2025.07.23]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2025.05.23...2025.07.23
[2025.05.23]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2025.03.21...2025.05.23
[2025.03.21]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2024.10.21...2025.03.21
[2024.10.21]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2024.10.01...2024.10.21
[2024.10.01]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2024.09.20...2024.10.01
[2024.09.20]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2024.04.23...2024.09.20
[2024.04.23]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2023.12.06...2024.04.23
[2023.12.06]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2023.05.31...2023.12.06
[2023.05.31]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2023.03.03...2023.05.31
[2023.03.03]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2023.02.24...2023.03.03
[2023.02.24]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/2022.11.18...2023.02.24
[2022.11.18]: https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote/compare/v2021.11...2022.11.18