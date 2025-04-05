# Changelog

All notable changes to the KMM OIDC Token Provider SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial implementation of TokenProvider
- TokenClient interface and Ktor implementation
- TokenRepository interface for storing tokens
- Platform-specific secure storage implementations for Android and iOS
- Support for auto login codes
- Support for multiple token scopes
- Reactive token state updates via Kotlin Flow
- Comprehensive documentation
- Example usage

## [1.0.0] - Future Release

### Added
- First stable release

[Unreleased]: https://github.com/username/Kmm-Oidc/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/username/Kmm-Oidc/releases/tag/v1.0.0