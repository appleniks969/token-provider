# Design Decisions

This document explains the key design decisions made during the development of the KMM OIDC Token Provider SDK.

## Architecture Decisions

### Clean Architecture Approach

**Decision**: Use a clean architecture with clear separation of concerns.

**Rationale**: 
- Improves testability
- Simplifies maintenance
- Makes it easier to adapt to changes in requirements
- Follows industry best practices

**Implementation**:
- **Core Domain**: Pure Kotlin models and interfaces
- **Client Layer**: Handles external communication
- **Repository Layer**: Handles data persistence
- **Platform Layer**: Platform-specific implementations

### Kotlin Multiplatform

**Decision**: Use Kotlin Multiplatform Mobile (KMM) for cross-platform development.

**Rationale**:
- Share business logic between Android and iOS
- Maintain native performance and platform-specific features
- Reduce code duplication
- Leverage Kotlin's features

**Implementation**:
- Common code in `commonMain`
- Platform-specific code in `androidMain` and `iosMain`
- Use expect/actual where needed

## API Design Decisions

### Focus on Token Management

**Decision**: Focus exclusively on token management rather than the full OIDC authentication flow.

**Rationale**:
- Simplifies the API
- Aligns with specific requirements
- Reduces scope creep
- Allows for more focused testing

**Implementation**:
- Removed authentication flow handling
- Focused on token acquisition, refresh, and storage

### Result Wrapper Pattern

**Decision**: Use a sealed class `TokenResult<T>` for operation results.

**Rationale**:
- Type-safe error handling
- Explicit success/error paths
- Compatible with coroutines

**Implementation**:
```kotlin
sealed class TokenResult<out T> {
    data class Success<T>(val data: T) : TokenResult<T>()
    data class Error(val exception: Throwable) : TokenResult<Nothing>()
}
```

### Reactive State

**Decision**: Expose a reactive state flow for token state.

**Rationale**:
- Allows UI to react to token state changes
- Follows modern reactive patterns
- Compatible with Android and iOS via Kotlin Flow

**Implementation**:
```kotlin
val tokenState: StateFlow<TokenState>
```

## Naming Decisions

### Domain-Specific Naming

**Decision**: Use domain-specific names that clearly communicate intent.

**Rationale**:
- Improves code readability
- Makes the API more intuitive
- Follows domain-driven design principles

**Implementation**:
- `TokenProvider` instead of `OIDCProvider`
- `TokenEndpoints` instead of `OIDCConfig`
- `TokenSet` instead of `StoredTokens`
- `TokenScope` instead of `TokenKey`

### Repository Pattern

**Decision**: Use the repository pattern for data access.

**Rationale**:
- Clearly communicates intent
- Follows established patterns
- Provides a consistent abstraction over data sources

**Implementation**:
- `TokenRepository` interface
- `ScopedTokenRepository` extension
- Platform-specific implementations

## Security Decisions

### Secure Storage

**Decision**: Use platform-specific secure storage mechanisms.

**Rationale**:
- Better security than cross-platform solutions
- Utilizes hardware security features when available
- Follows platform security best practices

**Implementation**:
- Android: Keystore + encrypted SharedPreferences
- iOS: Keychain Services

### Token Expiration Handling

**Decision**: Add buffer time and account for clock skew when checking token expiration.

**Rationale**:
- Prevents edge cases where tokens expire during use
- Accounts for differences in client and server clocks
- Provides a better user experience

**Implementation**:
```kotlin
fun fromResponse(
    response: TokenResponse,
    bufferTimeSeconds: Int = 30,
    clockSkewSeconds: Int = 60
): TokenSet {
    val expiresAt = currentTimeInSeconds() + response.expiresIn - bufferTimeSeconds - clockSkewSeconds
    // ...
}
```

## Extensibility Decisions

### Scope-Based Storage

**Decision**: Support storing tokens with different scopes.

**Rationale**:
- Supports multi-scope applications
- Allows for more flexible token management
- Prepares for future requirements

**Implementation**:
- `TokenScope` class for identifying different token sets
- `ScopedTokenRepository` interface with scope support

### Abstract Base Classes

**Decision**: Use abstract base classes for common functionality.

**Rationale**:
- Reduces code duplication
- Simplifies implementation of new storage providers
- Maintains consistency across implementations

**Implementation**:
- `SecureTokenRepository` abstract class