# KMM OIDC Token Provider SDK - Architecture

## Overview

The KMM OIDC Token Provider SDK follows a clean, modular architecture designed for simplicity, security, and cross-platform compatibility. This document outlines the architecture, components, and interactions of the SDK.

## Architectural Principles

The SDK is built on the following principles:

1. **Simplicity First** - Simple API surface with sensible defaults
2. **Security by Design** - Follows OIDC security best practices
3. **Platform Agnostic Core** - Common business logic independent of platform
4. **Platform-Specific Adaptations** - Optimized implementations for each platform
5. **Reactive State** - Flow-based state updates for reactive UIs
6. **Minimal Dependencies** - Limited external dependencies for reduced footprint

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      Application                             │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      TokenProvider                           │
└───────────────────────────┬─────────────────────────────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
┌──────────────────┐ ┌─────────────┐ ┌───────────────┐
│  OIDC Discovery  │ │Token Service│ │Secure Storage │
└──────────────────┘ └─────────────┘ └───────┬───────┘
                                             │
                           ┌─────────────────┴────────────────┐
                           │                                  │
                           ▼                                  ▼
              ┌────────────────────────┐      ┌─────────────────────────┐
              │  Android Secure Storage│      │  iOS Secure Storage     │
              └────────────────────────┘      └─────────────────────────┘
```

## Core Components

### TokenProvider

The main entry point for the SDK, responsible for:
- Initializing the SDK components
- Managing the authentication flow
- Handling token acquisition and refresh
- Exposing a simple API for client applications

```kotlin
class TokenProvider(
    private val httpClient: HttpClient,
    private val storage: SecureStorage,
    private val coroutineScope: CoroutineScope,
    private val issuerUrl: String,
    private val clientId: String,
    private val clientSecret: String? = null
) {
    // Public API methods
    suspend fun getAccessToken(forceRefresh: Boolean = false): TokenResult<TokenSet>
    suspend fun getAutoLoginCode(): String?
    suspend fun requestAutoLoginCode(username: String, additionalParams: Map<String, String> = emptyMap()): TokenResult<String>
    
    // State flow
    val tokenState: StateFlow<TokenState>
    
    // Factory method
    companion object {
        fun create(...): TokenProvider
    }
}
```

### OIDC Discovery

Handles the discovery of OIDC endpoints from the well-known configuration:
- Fetches and parses the `.well-known/openid-configuration` document
- Caches the discovered endpoints
- Provides the token endpoint for token operations

### Token Service

Manages token operations:
- Acquires tokens using different grant types
- Refreshes tokens when they expire
- Handles token validation
- Manages token state

### SecureStorage

Interface for secure token storage with platform-specific implementations:
- Stores tokens securely
- Retrieves stored tokens
- Clears tokens when needed

```kotlin
interface SecureStorage {
    suspend fun saveTokens(tokens: TokenSet)
    suspend fun getTokens(): TokenSet?
    suspend fun clearTokens()
}
```

### Domain Models

#### TokenSet

Represents the complete set of tokens from an OIDC provider:

```kotlin
data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val scope: String?,
    val expiresAt: Long,
    val autoLoginCode: String? = null
)
```

#### TokenState

Represents the current state of tokens:

```kotlin
sealed class TokenState {
    object NoToken : TokenState()
    object Refreshing : TokenState()
    data class Valid(val tokens: TokenSet) : TokenState()
    data class Invalid(val message: String) : TokenState()
}
```

#### TokenResult

Generic result wrapper for token operations:

```kotlin
sealed class TokenResult<out T> {
    data class Success<T>(val data: T) : TokenResult<T>()
    data class Error(val exception: Throwable) : TokenResult<Nothing>()
}
```

## Platform-Specific Implementations

### Android

#### AndroidSecureStorage

Android implementation of SecureStorage using Android Keystore:
- Uses Android Keystore for key generation
- Encrypts tokens using AES/GCM
- Stores encrypted data in SharedPreferences

### iOS

#### IOSSecureStorage

iOS implementation of SecureStorage using iOS Keychain:
- Uses Keychain Services for secure storage
- Stores tokens as generic passwords
- Uses service and account attributes for identification

## Authentication Flows

The SDK supports the following authentication flows:

### Token Refresh

1. Application requests an access token
2. TokenProvider checks if a stored token exists and is valid
3. If the token is expired, TokenProvider uses the refresh token to get a new access token
4. The new token is validated, stored, and returned to the application

### Auto Login Code

1. Application requests an auto login code for a specific user
2. TokenProvider sends the request to the OIDC provider
3. The auto login code is returned and stored
4. Application shares the code with the user via another channel

## State Management

The SDK uses Kotlin Flows for reactive state management:
- TokenProvider exposes a StateFlow of TokenState
- Applications can observe the state flow to react to token state changes
- State updates are emitted when tokens are acquired, refreshed, or invalidated

## Error Handling

The SDK uses a Result pattern for error handling:
- TokenResult.Success for successful operations
- TokenResult.Error for failed operations
- Detailed error messages for debugging

## Extensibility

The SDK is designed to be extensible:
- Interfaces for key components allow for custom implementations
- Factory methods accept custom implementations
- Configuration options for different authentication scenarios

## Security Considerations

The SDK implements the following security measures:
- Secure storage of tokens using platform-specific mechanisms
- Token refresh to minimize access token lifetimes
- Support for PKCE (Proof Key for Code Exchange) for mobile apps
- Configurable token validation

## Future Enhancements

The following enhancements are planned for future releases:

1. **Enhanced Token Validation** - JWT signature validation and claims verification
2. **Multiple Authentication Flows** - Support for authorization code flow, client credentials, etc.
3. **Session Management** - Enhanced session state management
4. **Improved Error Handling** - More detailed error types
5. **Biometric Authentication** - Integration with biometric authentication for secure storage

## Conclusion

The architecture of the KMM OIDC Token Provider SDK provides a solid foundation for secure, cross-platform token management. Its modular design allows for flexibility and extensibility while maintaining a simple API surface for client applications.
