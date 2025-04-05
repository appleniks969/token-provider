# KMM OIDC Token Provider SDK API Reference

This document provides detailed information about the public API for the OIDC Token Provider SDK.

## TokenProvider

The main entry point for the SDK.

### Creation

```kotlin
// Create with dependencies
val tokenProvider = TokenProvider.create(
    client = tokenClient,
    repository = tokenRepository,
    coroutineScope = coroutineScope
)

// Android helper (in examples package)
val tokenProvider = AndroidExample.createTokenProvider(context)

// iOS helper (in examples package)
val tokenProvider = IOSExample.createTokenProvider()
```

### Methods

#### `discoverConfiguration`

Discovers the OIDC provider configuration from the well-known endpoint.

```kotlin
suspend fun discoverConfiguration(issuerUrl: String): TokenResult<TokenEndpoints>
```

**Parameters:**
- `issuerUrl`: Base URL of the OIDC provider (e.g., "https://example.auth0.com")

**Returns:**
- `TokenResult<TokenEndpoints>`: Success with endpoints or Error

---

#### `getAccessToken`

Gets a valid access token, refreshing if necessary.

```kotlin
suspend fun getAccessToken(
    clientId: String,
    clientSecret: String? = null,
    force: Boolean = false
): TokenResult<String>
```

**Parameters:**
- `clientId`: Client ID for the OIDC provider
- `clientSecret`: Optional client secret
- `force`: Whether to force refresh even if the token isn't expired

**Returns:**
- `TokenResult<String>`: Success with access token or Error

---

#### `refreshAccessToken`

Explicitly refreshes an access token using a refresh token.

```kotlin
suspend fun refreshAccessToken(
    clientId: String,
    refreshToken: String,
    clientSecret: String? = null,
    scope: String? = null
): TokenResult<TokenSet>
```

**Parameters:**
- `clientId`: Client ID for the OIDC provider
- `refreshToken`: Refresh token to use
- `clientSecret`: Optional client secret
- `scope`: Optional scope for the new token

**Returns:**
- `TokenResult<TokenSet>`: Success with new token set or Error

---

#### `getAutoLoginCode`

Gets a stored auto login code.

```kotlin
suspend fun getAutoLoginCode(): String?
```

**Returns:**
- Auto login code or null if not available

---

#### `requestAutoLoginCode`

Requests a new auto login code from the OIDC provider.

```kotlin
suspend fun requestAutoLoginCode(
    clientId: String,
    username: String,
    clientSecret: String? = null,
    additionalParams: Map<String, String> = emptyMap()
): TokenResult<String>
```

**Parameters:**
- `clientId`: Client ID for the OIDC provider
- `username`: Username for which to generate the code
- `clientSecret`: Optional client secret
- `additionalParams`: Additional parameters required by the provider

**Returns:**
- `TokenResult<String>`: Success with auto login code or Error

---

### Properties

#### `tokenState`

A flow of token state updates.

```kotlin
val tokenState: StateFlow<TokenState>
```

## ScopedTokenRepository

Interface for token storage with scope support.

### Methods

#### `saveTokens`

Saves tokens for a specific scope.

```kotlin
suspend fun saveTokens(scope: TokenScope, tokens: TokenSet)
```

---

#### `getTokens`

Gets tokens for a specific scope.

```kotlin
suspend fun getTokens(scope: TokenScope): TokenSet?
```

---

#### `getAllScopes`

Gets all available token scopes.

```kotlin
suspend fun getAllScopes(): List<TokenScope>
```

---

#### `clearTokens`

Clears tokens for a specific scope.

```kotlin
suspend fun clearTokens(scope: TokenScope)
```

---

#### `clearAllTokens`

Clears all stored tokens.

```kotlin
suspend fun clearAllTokens()
```

## TokenScope

Represents a scope for token storage.

### Creation

```kotlin
// Default scope
TokenScope.DEFAULT

// Client-specific
TokenScope.forClient("client-id")

// User-specific
TokenScope.forUser("client-id", "user-id")

// Scope-specific
TokenScope.forScope("client-id", "read profile", "user-id")

// Resource-specific
TokenScope.forResource("client-id", "api-resource", "user-id")

// Custom scope
TokenScope(
    clientId = "client-id",
    userId = "user-id",
    scope = "read write",
    resource = "api",
    purpose = "mobile-app"
)
```

## TokenResult

Sealed class for handling operation results.

### Variants

#### `Success`

Represents a successful operation.

```kotlin
data class Success<T>(val data: T) : TokenResult<T>()
```

#### `Error`

Represents a failed operation.

```kotlin
data class Error(val exception: Throwable) : TokenResult<Nothing>()
```

### Usage

```kotlin
when (val result = tokenProvider.getAccessToken("client-id")) {
    is TokenResult.Success -> {
        val token = result.data
        // Use the token
    }
    is TokenResult.Error -> {
        val error = result.exception
        // Handle the error
    }
}
```

## TokenState

Sealed class representing the current state of tokens.

### Variants

- `NoToken`: No tokens available
- `Refreshing`: Tokens are being refreshed
- `Valid`: Tokens are valid and available
- `Invalid`: Tokens are invalid or an error occurred

### Usage

```kotlin
tokenProvider.tokenState.collect { state ->
    when (state) {
        is TokenState.NoToken -> showLoginButton()
        is TokenState.Refreshing -> showLoadingIndicator()
        is TokenState.Valid -> {
            hideLoadingIndicator()
            // Use state.tokens if needed
        }
        is TokenState.Invalid -> showError(state.message)
    }
}
```