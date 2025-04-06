# KMM OIDC Token Provider SDK - Domain Model

This document describes the domain model for the OIDC Token Provider SDK, following domain-driven design principles.

## Core Domain

### Entities

1. **TokenSet**
   - **Description**: Represents the complete set of tokens from an OIDC provider
   - **Properties**:
     - `accessToken`: The access token used for resource access
     - `refreshToken`: Token used to obtain new access tokens
     - `tokenType`: Type of token (usually "Bearer")
     - `expiresAt`: Timestamp when the access token expires
     - `scope`: Space-separated list of authorized scopes
     - `autoLoginCode`: Optional auto-login code
   - **Enhancement Plans**:
     - Add `idToken` support for user identity information
     - Add claim extraction methods
     - Support different token types

2. **AuthState** (evolution of current TokenState)
   - **Description**: Represents the current authentication state
   - **States**:
     - `NoToken`: No token available
     - `Refreshing`: Token refresh in progress
     - `Valid`: Valid token available
     - `Invalid`: Token invalid or expired
   - **Enhancement Plans**:
     - Add user identity information when available
     - Add more granular error states
     - Include state transition metadata

### Value Objects

1. **TokenEndpoints**
   - **Description**: OIDC endpoints discovered from provider
   - **Properties**:
     - `issuer`: Issuer identifier
     - `tokenEndpoint`: Token endpoint URL
     - `jwksUri`: JWKS (JSON Web Key Set) URI
   - **Enhancement Plans**:
     - Expand to include all standard OIDC endpoints
     - Add support for extension endpoints

2. **TokenResponse**
   - **Description**: Response from token endpoint
   - **Properties**:
     - `accessToken`: The access token
     - `tokenType`: Type of token
     - `expiresIn`: Expiration time in seconds
     - `refreshToken`: Optional refresh token
     - `scope`: Authorized scopes
     - `autoLoginCode`: Optional auto-login code
   - **Enhancement Plans**:
     - Add `idToken` support
     - Add standard claim properties

3. **TokenError**
   - **Description**: Error response from token endpoint
   - **Properties**:
     - `error`: Error code
     - `errorDescription`: Human-readable error description
     - `errorUri`: URI with more error information
   - **Enhancement Plans**:
     - Create specialized error types
     - Add more detailed error metadata

4. **AuthResult** (evolution of TokenResult)
   - **Description**: Result wrapper for authentication operations
   - **Types**:
     - `Success<T>`: Successful operation with data
     - `Error`: Failed operation with exception
   - **Enhancement Plans**:
     - Add specialized error subtypes
     - Include more diagnostic information
     - Add retry capabilities

## Services

1. **TokenManager** (evolution of TokenProvider)
   - **Description**: Main service for token operations
   - **Responsibilities**:
     - Token acquisition and refresh
     - Authentication flow coordination
     - Token state management
   - **Enhancement Plans**:
     - Support for multiple authentication flows
     - Better error handling
     - More configurable behavior

2. **OidcDiscoveryService**
   - **Description**: Service for OIDC discovery operations
   - **Responsibilities**:
     - Discover OIDC endpoints
     - Cache discovered configuration
     - Handle discovery errors
   - **Enhancement Plans**:
     - Better caching mechanism
     - Support for manual configuration
     - Fallback strategies

3. **TokenValidator**
   - **Description**: Service for token validation
   - **Responsibilities**:
     - Validate token signatures
     - Check token expiration
     - Validate standard claims
   - **Enhancement Plans**:
     - Support different signature algorithms
     - Configurable validation rules
     - Detailed validation errors

4. **SecureStorage**
   - **Description**: Interface for secure token storage
   - **Responsibilities**:
     - Save tokens securely
     - Retrieve tokens
     - Clear tokens
   - **Enhancement Plans**:
     - Enhanced encryption
     - More storage options
     - Better error handling

## Platform-Specific Implementations

1. **AndroidSecureStorage**
   - **Description**: Android implementation of SecureStorage
   - **Implementation**: Uses Android Keystore
   - **Enhancement Plans**:
     - Better encryption
     - Support for biometric authentication
     - Improved error handling

2. **IOSSecureStorage**
   - **Description**: iOS implementation of SecureStorage
   - **Implementation**: Uses iOS Keychain
   - **Enhancement Plans**:
     - Better error handling
     - Support for biometric authentication
     - Improved encryption

## Domain Relationships

- **TokenManager** uses **OidcDiscoveryService** to discover endpoints
- **TokenManager** uses **SecureStorage** to store and retrieve tokens
- **TokenManager** uses **TokenValidator** to validate tokens
- **TokenManager** returns **AuthResult** with either **TokenSet** or errors
- **TokenManager** maintains and emits **AuthState** changes

This domain model provides a solid foundation for the enhanced OIDC Token Provider SDK, following domain-driven design principles with clearly defined entities, value objects, and services.
