# KMM OIDC Token Provider SDK Architecture

This document describes the architecture of the OIDC Token Provider SDK.

## Overview

The SDK follows a clean architecture approach with clear separation of concerns. It's designed to be flexible, extensible, and to follow best practices for Kotlin Multiplatform development.

## Components

### 1. TokenProvider

The main entry point for the SDK. It orchestrates the interaction between the client and repository components.

**Responsibilities:**
- Discovering OIDC endpoints
- Managing token lifecycle
- Exposing a simple API for consumers

**Key Methods:**
- `discoverConfiguration()` - Fetches OIDC provider metadata
- `getAccessToken()` - Gets a valid access token, refreshing if needed
- `refreshAccessToken()` - Explicitly refreshes an access token
- `requestAutoLoginCode()` - Requests an auto login code
- `getAutoLoginCode()` - Gets a stored auto login code

### 2. TokenClient

Handles HTTP communication with the OIDC provider.

**Responsibilities:**
- Making HTTP requests to OIDC endpoints
- Parsing responses into domain models
- Handling network errors

**Key Implementations:**
- `KtorTokenClient` - Uses Ktor for HTTP requests

### 3. TokenRepository

Manages token persistence.

**Responsibilities:**
- Securely storing tokens
- Retrieving stored tokens
- Managing token scopes

**Key Implementations:**
- `InMemoryTokenRepository` - For testing
- `AndroidSecureRepository` - Android-specific implementation using Keystore
- `IOSSecureRepository` - iOS-specific implementation using Keychain

### 4. Domain Models

Value objects that represent core SDK concepts.

**Key Models:**
- `TokenEndpoints` - OIDC provider endpoints
- `TokenSet` - Set of tokens (access, refresh, etc.)
- `TokenResponse` - Response from token endpoint
- `TokenScope` - Identifier for token storage
- `TokenResult` - Result wrapper for operations
- `TokenState` - Current state of tokens

## Flow Diagrams

### Token Discovery Flow

```
App -> TokenProvider: discoverConfiguration(issuerUrl)
TokenProvider -> TokenClient: discoverConfiguration(issuerUrl)
TokenClient -> OIDC Provider: GET /.well-known/openid-configuration
OIDC Provider -> TokenClient: Configuration JSON
TokenClient -> TokenProvider: TokenEndpoints
TokenProvider -> App: TokenResult<TokenEndpoints>
```

### Token Refresh Flow

```
App -> TokenProvider: getAccessToken()
TokenProvider -> TokenRepository: getTokens()
TokenRepository -> TokenProvider: TokenSet
TokenProvider: [Check if token expired]
TokenProvider -> TokenClient: refreshToken()
TokenClient -> OIDC Provider: POST /token
OIDC Provider -> TokenClient: TokenResponse
TokenClient -> TokenProvider: TokenSet
TokenProvider -> TokenRepository: saveTokens(TokenSet)
TokenProvider -> App: TokenResult<String>
```

## Platform-Specific Implementations

The SDK uses Kotlin Multiplatform's expect/actual pattern to provide platform-specific implementations where needed:

- **Android:** Uses Android Keystore and SharedPreferences for secure storage
- **iOS:** Uses Keychain Services for secure storage

## Dependencies

- **Ktor:** For HTTP client functionality
- **Kotlinx Serialization:** For JSON serialization/deserialization
- **Kotlinx Coroutines:** For asynchronous operations

## Extension Points

The SDK can be extended in several ways:

1. Custom TokenClient implementations
2. Custom TokenRepository implementations
3. Additional token-related features

## Security Considerations

- Tokens are stored using platform-specific secure storage mechanisms
- Communications use HTTPS
- Token expiration is handled with buffer times to account for clock skew
- Refresh tokens are handled securely