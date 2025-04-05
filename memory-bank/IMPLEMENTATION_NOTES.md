# Implementation Notes

This document provides important details about the implementation of the KMM OIDC Token Provider SDK.

## Kotlin Multiplatform Structure

The SDK uses a standard Kotlin Multiplatform structure:

- `commonMain`: Contains shared code for all platforms
- `androidMain`: Contains Android-specific implementations
- `iosMain`: Contains iOS-specific implementations

Each module uses the appropriate platform-specific APIs while sharing as much code as possible.

## HTTP Requests with Ktor

The SDK uses Ktor for all HTTP communication with the OIDC provider.

### Key Implementation Points

1. **Content Negotiation**: Uses Kotlinx Serialization for JSON parsing
   ```kotlin
   install(ContentNegotiation) {
       json(json)
   }
   ```

2. **Timeout Handling**: Sets reasonable timeouts for network operations
   ```kotlin
   install(HttpTimeout) {
       requestTimeoutMillis = 30000
       connectTimeoutMillis = 15000
       socketTimeoutMillis = 15000
   }
   ```

3. **Form Submission**: Uses `submitForm` for token endpoint requests as required by the OAuth 2.0 specification
   ```kotlin
   httpClient.submitForm(
       url = tokenEndpoint,
       formParameters = parameters
   )
   ```

4. **Error Handling**: Properly parses error responses according to the OAuth 2.0 specification
   ```kotlin
   val error: TokenError = response.body()
   TokenResult.Error(Exception(error.errorDescription ?: error.error))
   ```

## Secure Storage

### Android Implementation

The Android secure storage implementation uses:

1. **Android Keystore System**: To store an encryption key securely
   ```kotlin
   KeyGenParameterSpec.Builder(
       keyAlias,
       KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
   )
   ```

2. **AES-GCM Encryption**: For authenticated encryption of the token data
   ```kotlin
   val cipher = Cipher.getInstance("AES/GCM/NoPadding")
   ```

3. **SharedPreferences**: To store the encrypted data
   ```kotlin
   sharedPreferences.edit().putString(key, encryptedData).apply()
   ```

### iOS Implementation

The iOS secure storage implementation uses:

1. **Keychain Services API**: To securely store sensitive data
   ```kotlin
   val query = mutableMapOf<Any?, Any?>(
       kSecClass to kSecClassGenericPassword,
       kSecAttrService to serviceName,
       kSecAttrAccount to key,
       kSecValueData to nsData,
       kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
   )
   ```

2. **Appropriate Access Control**: To ensure data is only accessible in the right context
   ```kotlin
   kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
   ```

## Token Refresh Logic

The token refresh logic follows these steps:

1. Check if the token is expired (with buffer time and clock skew)
   ```kotlin
   if (force || storedTokens.isExpired()) {
       // Refresh token
   }
   ```

2. Use the refresh token to get a new access token
   ```kotlin
   val refreshResult = client.refreshToken(
       tokenEndpoint = config.tokenEndpoint,
       clientId = clientId,
       refreshToken = refreshToken,
       clientSecret = clientSecret,
       scope = scope
   )
   ```

3. Save the new tokens and update the state
   ```kotlin
   if (result is TokenResult.Success) {
       repository.saveTokens(result.data)
   }
   ```

## Auto Login Code Implementation

The auto login code implementation:

1. Requests the code from the OIDC provider
   ```kotlin
   val result = client.requestAutoLoginCode(
       tokenEndpoint = config.tokenEndpoint,
       clientId = clientId,
       username = username,
       clientSecret = clientSecret,
       additionalParams = additionalParams
   )
   ```

2. Updates any existing tokens with the new code
   ```kotlin
   repository.getTokens()?.let { tokens ->
       val updatedTokens = tokens.copy(autoLoginCode = result.data)
       repository.saveTokens(updatedTokens)
   }
   ```

## Coroutine Usage

The SDK uses Kotlin coroutines for asynchronous operations:

1. **Dispatchers.IO**: For I/O-bound operations like network requests and disk access
   ```kotlin
   withContext(Dispatchers.IO) {
       // Network or disk operations
   }
   ```

2. **StateFlow**: For reactive state updates
   ```kotlin
   private val _tokenState = MutableStateFlow<TokenState>(TokenState.NoToken)
   override val tokenState: Flow<TokenState> = _tokenState
   ```

3. **Scoping**: Coroutine operations are scoped appropriately
   ```kotlin
   val tokenState: StateFlow<TokenState> = client.tokenState.stateIn(
       scope = coroutineScope,
       started = SharingStarted.Eagerly,
       initialValue = TokenState.NoToken
   )
   ```

## Threading Considerations

- All network and disk operations are performed off the main thread
- State updates are thread-safe through the use of atomic operations and coroutines
- Platform-specific code handles threading appropriately for each platform

## Error Handling

The SDK uses a robust error handling approach:

1. All operations return a `TokenResult` with clear success and error paths
2. Specific exceptions are caught and wrapped with meaningful error messages
3. Network errors are properly handled and translated to user-friendly messages
4. Token state reflects error conditions for reactive UI updates