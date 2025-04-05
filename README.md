# KMM OIDC Token Provider SDK

A streamlined Kotlin Multiplatform Mobile (KMM) SDK for managing OpenID Connect (OIDC) tokens on both Android and iOS platforms. This SDK provides a simplified API for obtaining and refreshing tokens with minimal configuration.

## Features

- **Simple Token Management** - Get tokens with a single method call
- **Automatic Discovery** - No need to manually handle OIDC discovery
- **Auto Refresh** - Tokens are automatically refreshed when needed
- **Secure Storage** - Platform-specific secure storage for tokens
- **Auto Login Code Support** - Request and store auto login codes
- **Reactive State** - Kotlin Flow-based state updates

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
// Create the TokenProvider
val tokenProvider = AndroidExample.createTokenProvider(
    context = applicationContext,
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
// Create the TokenProvider
let tokenProvider = IOSExampleKt.createTokenProvider(
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

## Auto Login Code Support

```kotlin
// Android
lifecycleScope.launch {
    when (val result = tokenProvider.requestAutoLoginCode(
        username = "user@example.com",
        additionalParams = mapOf("redirect_uri" to "myapp://callback")
    )) {
        is TokenResult.Success -> {
            val code = result.data
            // Use the auto login code
        }
        is TokenResult.Error -> {
            // Handle error
        }
    }
}

// iOS
TokenProviderKt.requestAutoLoginCode(
    tokenProvider,
    username: "user@example.com",
    additionalParams: ["redirect_uri": "myapp://callback"]
) { result in
    // Handle result
}
```

## Token State Observation

```kotlin
// Observe token state changes
lifecycleScope.launch {
    tokenProvider.tokenState.collect { state ->
        when (state) {
            is TokenState.NoToken -> { /* No token available */ }
            is TokenState.Refreshing -> { /* Token is being refreshed */ }
            is TokenState.Valid -> { /* Token is valid */ }
            is TokenState.Invalid -> { /* Token is invalid */ }
        }
    }
}
```

## Architecture

The SDK follows a simplified architecture with minimal abstractions:

- **TokenProvider** - Main entry point that handles discovery, token acquisition, and refreshing
- **SecureStorage** - Interface for platform-specific secure token storage
  - **AndroidSecureStorage** - Android implementation using Keystore
  - **IOSSecureStorage** - iOS implementation using Keychain

## API Reference

### TokenProvider

```kotlin
// Create with factory method
val tokenProvider = TokenProvider.create(
    engine = httpEngine,
    storage = secureStorage,
    coroutineScope = coroutineScope,
    issuerUrl = "https://example.auth0.com",
    clientId = "your-client-id",
    clientSecret = "your-client-secret" // Optional
)

// Get tokens (handles discovery and refresh)
val result = tokenProvider.getAccessToken()

// Get auto login code
val code = tokenProvider.getAutoLoginCode()

// Request new auto login code
val result = tokenProvider.requestAutoLoginCode(username)

// Observe token state
tokenProvider.tokenState.collect { state -> ... }
```

### TokenSet

The `TokenSet` class represents a full set of tokens:

```kotlin
data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val scope: String?,
    val expiresAt: Long,
    val autoLoginCode: String?
)
```

## Security Considerations

- Tokens are stored using platform-specific secure storage mechanisms
- Android: Keystore + encrypted SharedPreferences
- iOS: Keychain Services
- Token expiration includes buffer time and clock skew handling

## License

```
Copyright (c) 2025 Example Organization

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```