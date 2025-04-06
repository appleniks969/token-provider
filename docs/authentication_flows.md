# KMM OIDC Token Provider SDK - Authentication Flows

## Overview

This document describes the OpenID Connect (OIDC) authentication flows supported by the SDK. The SDK is designed to simplify authentication while maintaining security best practices.

## Supported Authentication Flows

The KMM OIDC Token Provider SDK supports the following authentication flows:

1. **Token Refresh Flow** - Refresh an existing access token using a refresh token
2. **Auto Login Code Flow** - Request and use an auto login code for simplified authentication
3. **Future Support** - Authorization Code Flow, Client Credentials Flow, Password Grant Flow

## Token Refresh Flow

The Token Refresh Flow allows applications to obtain a new access token when the current one expires, without requiring the user to re-authenticate.

### Flow Diagram

```
┌──────────────┐                                     ┌────────────────┐
│              │                                     │                │
│              │─────1. Request Access Token────────▶│                │
│              │                                     │                │
│              │                                     │  TokenProvider │
│  Application │◀────2. Check Stored Tokens─────────│                │
│              │                                     │                │
│              │                                     │                │
│              │                                     └────────┬───────┘
│              │                                              │
│              │                                              │
│              │                                     ┌────────▼───────┐
│              │                                     │                │
│              │                                     │  OIDC Provider │
│              │◀────3. Token Response──────────────│                │
└──────────────┘                                     └────────────────┘
```

### Steps

1. The application requests an access token from the TokenProvider.
2. The TokenProvider checks if it has stored tokens.
   - If no tokens are stored, it returns an error.
   - If tokens are stored but not expired, it returns the existing access token.
   - If tokens are stored but the access token is expired, it proceeds with a refresh.
3. The TokenProvider sends a token refresh request to the OIDC provider.
4. The OIDC provider validates the refresh token and issues a new access token.
5. The TokenProvider stores the new tokens and returns the access token to the application.

### Implementation

```kotlin
// Android
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

// iOS
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

### Security Considerations

- Refresh tokens are stored securely using platform-specific secure storage.
- The SDK includes a buffer time for token expiration to ensure tokens are refreshed before they actually expire.
- If the refresh token is invalid or expired, the application must re-authenticate the user.

## Auto Login Code Flow

The Auto Login Code Flow provides a simplified way to authenticate users, often used for device activation or quick login scenarios.

### Flow Diagram

```
┌──────────────┐                                     ┌────────────────┐
│              │                                     │                │
│              │─────1. Request Auto Login Code─────▶│                │
│              │                                     │  TokenProvider │
│  Application │◀────2. Auto Login Code─────────────│                │
│              │                                     │                │
│              │─────3. User enters code in         └────────┬───────┘
│              │       another device                         │
│              │                                              │
│              │                                     ┌────────▼───────┐
│              │                                     │                │
│              │                                     │  OIDC Provider │
│              │◀────4. Authentication Complete──────│                │
└──────────────┘                                     └────────────────┘
```

### Steps

1. The application requests an auto login code for a specific user.
2. The TokenProvider sends the request to the OIDC provider.
3. The OIDC provider generates an auto login code and returns it.
4. The application displays or shares the code with the user.
5. The user enters the code on another device or in another application.
6. The code is exchanged for tokens, and authentication is completed.

### Implementation

```kotlin
// Request an auto login code
lifecycleScope.launch {
    when (val result = tokenProvider.requestAutoLoginCode(
        username = "user@example.com",
        additionalParams = mapOf("redirect_uri" to "myapp://callback")
    )) {
        is TokenResult.Success -> {
            val code = result.data
            // Share the code with the user
        }
        is TokenResult.Error -> {
            // Handle error
        }
    }
}

// Get a stored auto login code if available
lifecycleScope.launch {
    val code = tokenProvider.getAutoLoginCode()
    if (code != null) {
        // Use the stored code
    }
}
```

### Security Considerations

- Auto login codes typically have a short lifetime.
- The code should be transmitted to the user via a secure channel.
- The application should validate that the user has the authority to request an auto login code.

## Future Authentication Flows

### Authorization Code Flow with PKCE

The SDK will support the Authorization Code Flow with PKCE (Proof Key for Code Exchange), which is recommended for mobile applications.

#### Flow Diagram

```
┌──────────────┐                                     ┌────────────────┐
│              │                                     │                │
│              │─────1. Initiate Auth (with PKCE)───▶│                │
│              │                                     │  OIDC Provider │
│  Application │◀────2. Authorization Code──────────│                │
│              │                                     │                │
│              │─────3. Exchange Code + Verifier────▶│                │
│              │                                     │                │
│              │◀────4. Token Response──────────────│                │
└──────────────┘                                     └────────────────┘
```

#### Implementation (Future)

```kotlin
// Will be implemented in a future release
val authConfig = AuthConfig.Builder()
    .issuerUrl("https://example.auth0.com")
    .clientId("your-client-id")
    .redirectUri("myapp://callback")
    .scopes(listOf("openid", "profile", "email"))
    .build()

lifecycleScope.launch {
    when (val result = tokenProvider.authorize(authConfig)) {
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

### Client Credentials Flow

The SDK will support the Client Credentials Flow for server-to-server authentication, where no user is involved.

#### Flow Diagram

```
┌──────────────┐                                     ┌────────────────┐
│              │                                     │                │
│              │─────1. Client Credentials─────────▶│                │
│              │                                     │  OIDC Provider │
│  Application │◀────2. Access Token────────────────│                │
│              │                                     │                │
└──────────────┘                                     └────────────────┘
```

#### Implementation (Future)

```kotlin
// Will be implemented in a future release
val authConfig = AuthConfig.Builder()
    .issuerUrl("https://example.auth0.com")
    .clientId("your-client-id")
    .clientSecret("your-client-secret")
    .scopes(listOf("api:access"))
    .build()

lifecycleScope.launch {
    when (val result = tokenProvider.clientCredentials(authConfig)) {
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

### Resource Owner Password Flow

The SDK will support the Resource Owner Password Flow for legacy applications where direct username/password authentication is required.

#### Flow Diagram

```
┌──────────────┐                                     ┌────────────────┐
│              │                                     │                │
│              │─────1. Username/Password───────────▶│                │
│              │                                     │  OIDC Provider │
│  Application │◀────2. Token Response──────────────│                │
│              │                                     │                │
└──────────────┘                                     └────────────────┘
```

#### Implementation (Future)

```kotlin
// Will be implemented in a future release
lifecycleScope.launch {
    when (val result = tokenProvider.passwordGrant(
        username = "user@example.com",
        password = "securepassword",
        scopes = listOf("openid", "profile", "email")
    )) {
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

## Selecting the Right Flow

| Flow | Use Case | Security Level | User Experience | Notes |
|------|----------|---------------|----------------|-------|
| Token Refresh | Maintaining sessions | High | Seamless | Requires initial authentication |
| Auto Login Code | Device activation, quick login | Medium | Simple | Good for cross-device auth |
| Authorization Code with PKCE | Mobile apps | High | Standard | Recommended for mobile |
| Client Credentials | Server-to-server | Medium | N/A | No user involvement |
| Resource Owner Password | Legacy systems | Low | Simple | Use only when necessary |

## Error Handling

The SDK provides a consistent error handling pattern across all authentication flows:

```kotlin
when (val result = tokenProvider.getAccessToken()) {
    is TokenResult.Success -> {
        // Handle success
    }
    is TokenResult.Error -> {
        when (result.exception) {
            is IOException -> // Network error
            is SecurityException -> // Security error
            is IllegalStateException -> // State error
            else -> // Unknown error
        }
    }
}
```

## Best Practices

1. **Always use PKCE** for mobile applications (when implemented)
2. **Store tokens securely** using the SDK's secure storage
3. **Handle token refresh errors** gracefully
4. **Implement proper error UX** for authentication failures
5. **Use appropriate scopes** to limit token privileges
6. **Clear tokens** on logout
7. **Monitor token state** using the SDK's state flow

## Conclusion

The KMM OIDC Token Provider SDK simplifies OIDC authentication flows while maintaining security best practices. The current implementation supports token refresh and auto login code flows, with more authentication flows planned for future releases.
