# KMM OIDC Token Provider SDK

A streamlined Kotlin Multiplatform Mobile (KMM) SDK for managing OpenID Connect (OIDC) tokens on both Android and iOS platforms. This SDK provides a simplified API for obtaining and refreshing tokens with minimal configuration.

## Features

- **Simple Token Management** - Get tokens with a single method call
- **Automatic Discovery** - No need to manually handle OIDC discovery
- **Auto Refresh** - Tokens are automatically refreshed when needed
- **Secure Storage** - Platform-specific secure storage for tokens
- **Auto Login Code Support** - Request and store auto login codes
- **Reactive State** - Kotlin Flow-based state updates
- **Enhanced Security** - Supports PKCE and token validation
- **Multiple Auth Flows** - Authorization Code, Client Credentials, and more
- **Cross-Platform** - Works seamlessly on both Android and iOS

## Installation

### Gradle Setup (Android)

Add the following to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.example:oidc:1.0.0")
}
```

### CocoaPods Setup (iOS)

Add the following to your `Podfile`:

```ruby
pod 'OidcTokenProvider', '~> 1.0.0'
```

## Quick Start

### Android

```kotlin
// Create secure storage implementation
val storage = AndroidSecureStorage(context)

// Create the TokenProvider
val tokenProvider = TokenProvider.create(
    engine = Android.create(),
    storage = storage,
    coroutineScope = lifecycleScope,
    issuerUrl = "https://example.auth0.com",
    clientId = "your-client-id",
    clientSecret = "your-client-secret" // Optional
)

// Get an access token - discovery and refresh are handled automatically
lifecycleScope.launch {
    when (val result = tokenProvider.getAccessToken()) {
        is TokenResult.Success -> {
            val tokenSet = result.data
            // Use tokenSet.accessToken for API calls
        }
        is TokenResult.Error -> {
            // Handle error
        }
    }
}
```

### iOS

```swift
// Create dependencies
let storage = IOSSecureStorage()
let engine = DarwinHttpClientEngine()
let scope = MainScope()

// Create the TokenProvider
let tokenProvider = TokenProviderKt.create(
    engine: engine,
    storage: storage,
    coroutineScope: scope,
    issuerUrl: "https://example.auth0.com",
    clientId: "your-client-id",
    clientSecret: "your-client-secret" // Optional
)

// Get an access token - discovery and refresh are handled automatically
TokenProviderKt.getAccessToken(tokenProvider) { result in
    switch result {
    case is TokenResult.Success<TokenSet>:
        let success = result as! TokenResult.Success<TokenSet>
        let tokens = success.data
        // Use tokens.accessToken for API calls
        
    case is TokenResult.Error:
        let error = result as! TokenResult.Error
        // Handle error
        
    default:
        break
    }
}
```

## Documentation

For more detailed information, refer to the following documentation:

- [Architecture and Design](docs/architecture.md)
- [Authentication Flows](docs/authentication_flows.md)
- [API Reference](docs/api_reference.md)
- [Best Practices](docs/best_practices.md)
- [Android Integration](docs/android_integration.md)
- [iOS Integration](docs/ios_integration.md)

## Architecture

The SDK follows a minimalist architecture with a focus on ease of use:

- **TokenProvider** - Main entry point that handles discovery, token acquisition, and refreshing
- **SecureStorage** - Interface for platform-specific secure token storage
  - **AndroidSecureStorage** - Android implementation using Keystore
  - **IOSSecureStorage** - iOS implementation using Keychain

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

## License

Apache License 2.0 - see [LICENSE](LICENSE) file for details
