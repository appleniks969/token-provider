# KMM OIDC Token Provider SDK

## Overview

The KMM OIDC Token Provider SDK is a Kotlin Multiplatform Mobile (KMM) library that simplifies OpenID Connect (OIDC) authentication and token management for both Android and iOS applications. It provides a unified API for handling authentication flows, token management, and secure storage across platforms.

## Key Features

- **Cross-Platform Compatibility** - Single codebase for Android and iOS
- **Comprehensive Authentication Flows** - Support for multiple OIDC flows
- **Secure Token Storage** - Platform-specific secure storage implementations
- **Automatic Token Management** - Handles token acquisition, validation, and refresh
- **JWT Validation** - Comprehensive token validation
- **PKCE Support** - Enhanced security with PKCE
- **Reactive State Management** - Flow-based state updates

## Architecture

The SDK follows a clean architecture approach with clear separation of concerns:

- **Public API Layer** - Main entry points for application developers
- **Service Layer** - Core business logic and services
- **Infrastructure Layer** - Platform-agnostic infrastructure components
- **Platform-Specific Layer** - Platform-specific implementations

## Getting Started

To use the SDK in your project:

### Gradle (Android)

```kotlin
dependencies {
    implementation("com.example:oidc:2.0.0")
}
```

### CocoaPods (iOS)

```ruby
pod 'OidcTokenProvider', '~> 2.0.0'
```

## Basic Usage

```kotlin
// Android
val tokenManager = TokenManager.Builder()
    .config(
        AuthConfig.Builder()
            .issuerUrl("https://example.auth0.com")
            .clientId("your-client-id")
            .build()
    )
    .storage(AndroidSecureStorage(context))
    .engine(AndroidHttpEngine())
    .coroutineScope(lifecycleScope)
    .build()

// Get an access token
lifecycleScope.launch {
    when (val result = tokenManager.getAccessToken()) {
        is AuthResult.Success -> {
            val tokenSet = result.data
            // Use tokenSet.accessToken for API calls
        }
        is AuthResult.Error -> {
            // Handle error
        }
    }
}

// iOS (Kotlin)
val tokenManager = TokenManager.Builder()
    .config(
        AuthConfig.Builder()
            .issuerUrl("https://example.auth0.com")
            .clientId("your-client-id")
            .build()
    )
    .storage(IOSSecureStorage())
    .engine(DarwinHttpClientEngine())
    .coroutineScope(MainScope())
    .build()

// iOS (Swift)
// See iOS-specific documentation for Swift usage
```

## Project Status

The SDK is currently under active development. See the [Implementation Plan](implementation_plan.md) for details on the current status and roadmap.

## Documentation

- [Architecture](architecture.md)
- [API Reference](api_reference.md)
- [Authentication Flows](authentication_flows.md)
- [Android Integration](android_integration.md)
- [iOS Integration](ios_integration.md)
- [Best Practices](best_practices.md)
- [Implementation Plan](implementation_plan.md)
- [Task Status](task_status.md)
