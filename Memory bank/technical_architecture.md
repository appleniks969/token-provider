# KMM OIDC Token Provider SDK - Technical Architecture

This document describes the technical architecture of the enhanced OIDC Token Provider SDK, including components, interactions, and implementation details.

## Architecture Overview

The SDK follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                      Public API Layer                        │
│                                                             │
│                      TokenManager                           │
└───────────────────────────────┬─────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                           │
│                                                             │
│   ┌─────────────────┐   ┌────────────┐   ┌──────────────┐   │
│   │OidcDiscovery    │   │Token       │   │Auth          │   │
│   │Service          │   │Validator   │   │StateManager  │   │
│   └─────────────────┘   └────────────┘   └──────────────┘   │
└───────────────────────────────┬─────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                        │
│                                                             │
│   ┌─────────────────┐   ┌────────────┐   ┌──────────────┐   │
│   │HttpClient       │   │Secure      │   │Session       │   │
│   │                 │   │Storage     │   │Management    │   │
│   └─────────────────┘   └────────────┘   └──────────────┘   │
└───────────────────────────────┬─────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                  Platform-Specific Layer                     │
│                                                             │
│   ┌─────────────────────┐      ┌────────────────────────┐   │
│   │Android              │      │iOS                     │   │
│   │Implementation       │      │Implementation          │   │
│   └─────────────────────┘      └────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Component Descriptions

### Public API Layer

#### TokenManager

The main entry point for the SDK, providing a simple interface for token operations.

```kotlin
class TokenManager(
    private val discoveryService: OidcDiscoveryService,
    private val tokenValidator: TokenValidator,
    private val stateManager: AuthStateManager,
    private val storage: SecureStorage,
    private val config: AuthConfig
) {
    // Public methods for token operations
    suspend fun getAccessToken(forceRefresh: Boolean = false): AuthResult<TokenSet>
    suspend fun authorize(parameters: Map<String, String>): AuthResult<TokenSet>
    suspend fun logout(): AuthResult<Unit>
    val authState: StateFlow<AuthState>
}
```

### Service Layer

#### OidcDiscoveryService

Handles OIDC discovery operations, caching, and configuration.

```kotlin
class OidcDiscoveryService(
    private val httpClient: HttpClient,
    private val storage: SecureStorage,
    private val config: AuthConfig
) {
    suspend fun discoverConfiguration(): AuthResult<OidcConfiguration>
    suspend fun getConfiguration(): OidcConfiguration?
    suspend fun clearCache()
}
```

#### TokenValidator

Validates tokens and extracts claims.

```kotlin
class TokenValidator(
    private val httpClient: HttpClient,
    private val config: AuthConfig
) {
    suspend fun validateToken(token: String): AuthResult<Boolean>
    fun extractClaims(token: String): Map<String, Any>
    suspend fun validateTokenSignature(token: String): AuthResult<Boolean>
}
```

#### AuthStateManager

Manages and emits authentication state changes.

```kotlin
class AuthStateManager(
    private val storage: SecureStorage,
    private val coroutineScope: CoroutineScope
) {
    val state: StateFlow<AuthState>
    fun updateState(newState: AuthState)
    fun resetState()
}
```

### Infrastructure Layer

#### HttpClient

Abstraction for HTTP operations with default implementation using Ktor.

```kotlin
interface HttpClient {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun post(url: String, body: Any, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun submitForm(url: String, parameters: Map<String, String>): HttpResponse
}

class KtorHttpClient(private val engine: HttpClientEngine) : HttpClient {
    // Implementation using Ktor
}
```

#### SecureStorage

Interface for secure token storage with platform-specific implementations.

```kotlin
interface SecureStorage {
    suspend fun saveTokens(tokens: TokenSet)
    suspend fun getTokens(): TokenSet?
    suspend fun clearTokens()
    
    // Enhanced methods
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}
```

#### SessionManagement

Handles user session tracking and expiration.

```kotlin
class SessionManager(
    private val storage: SecureStorage,
    private val config: AuthConfig,
    private val coroutineScope: CoroutineScope
) {
    val sessionState: StateFlow<SessionState>
    suspend fun startSession(tokenSet: TokenSet)
    suspend fun endSession()
    suspend fun refreshSession(tokenSet: TokenSet)
}
```

### Platform-Specific Layer

#### Android Implementation

```kotlin
class AndroidSecureStorage(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : SecureStorage {
    // Implementation using Android Keystore
}

class AndroidHttpEngine : HttpClientEngine {
    // Android-specific HTTP implementation
}
```

#### iOS Implementation

```kotlin
class IOSSecureStorage(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : SecureStorage {
    // Implementation using iOS Keychain
}

class IOSHttpEngine : HttpClientEngine {
    // iOS-specific HTTP implementation
}
```

## Authentication Flows

### Authorization Code Flow

1. TokenManager receives authorization request
2. Generates state and PKCE parameters
3. Directs user to authorization endpoint
4. Captures redirect with authorization code
5. Exchanges code for tokens
6. Validates and stores tokens
7. Updates AuthState

### Client Credentials Flow

1. TokenManager receives credentials request
2. Sends client credentials to token endpoint
3. Receives access token
4. Validates and stores token
5. Updates AuthState

### Resource Owner Password Flow

1. TokenManager receives password grant request
2. Sends username/password to token endpoint
3. Receives tokens
4. Validates and stores tokens
5. Updates AuthState

## Token Validation

1. TokenValidator receives token
2. Decodes JWT structure
3. Validates expiration time
4. Validates signature against JWKS
5. Validates required claims (iss, aud, etc.)
6. Returns validation result

## Conclusion

This technical architecture provides a robust foundation for the OIDC Token Provider SDK, with clear separation of concerns, well-defined interfaces, and support for multiple authentication flows. The architecture is designed to be extensible, maintainable, and platform-independent while leveraging platform-specific optimizations where appropriate.
