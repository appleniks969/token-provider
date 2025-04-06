# KMM OIDC Token Provider SDK - Project Overview

## Current Implementation

The Kotlin Multiplatform Mobile (KMM) OIDC Token Provider SDK currently provides a simplified API for obtaining and refreshing OpenID Connect tokens on both Android and iOS platforms. 

### Existing Components

1. **TokenProvider**
   - Main entry point for token operations
   - Handles discovery, token acquisition, and refresh
   - Provides access to token state

2. **Domain Models**
   - TokenSet: Stores access token, refresh token, and metadata
   - TokenEndpoints: OIDC endpoints from discovery
   - TokenResponse/TokenError: API response models
   - TokenState/TokenResult: State and result wrappers

3. **Storage Interface**
   - SecureStorage: Interface for token persistence
   - AndroidSecureStorage: Android implementation using Keystore
   - IOSSecureStorage: iOS implementation using Keychain

### Current Limitations

- Limited support for OIDC authentication flows
- No JWT validation
- Limited claim extraction capabilities
- No PKCE support
- Basic error handling
- Limited session management

## Enhanced Architecture

Our enhanced architecture will address these limitations while maintaining backward compatibility:

### Core Components

1. **AuthConfig**
   - Configuration for OIDC provider and flows
   - PKCE configuration
   - Builder pattern for easy setup

2. **TokenManager** (evolution of TokenProvider)
   - Enhanced token operations
   - Support for multiple authentication flows
   - JWT validation

3. **OidcDiscoveryService**
   - Dedicated service for discovery operations
   - Improved caching and error handling

4. **TokenValidator**
   - JWT validation logic
   - Claim extraction utilities
   - Standard compliance checks

5. **AuthStateManager**
   - Enhanced state management
   - Flow-based updates
   - More granular state representation

6. **SecureStorage** (existing)
   - Enhanced encryption
   - Better error handling

7. **HttpClient**
   - Abstraction for network operations
   - Configurable for testing

8. **Session Management**
   - User session tracking
   - Session expiration handling
   - Single sign-out support

### Domain Models

1. **AuthState** (evolved TokenState)
   - More comprehensive state representation
   - User information integration

2. **TokenSet** (enhanced)
   - ID token support
   - Claim access methods
   - Better type safety

3. **OidcConfiguration**
   - Complete OIDC discovery model
   - Additional endpoint support

4. **AuthResult** (evolved TokenResult)
   - Specialized error types
   - Detailed error information

### Platform-Specific Components

1. **Android Module**
   - AndroidSecureStorage
   - Android-specific auth integrations

2. **iOS Module**
   - IOSSecureStorage
   - iOS-specific auth integrations
   - Swift-friendly API

## Implementation Approach

We'll follow a phased approach to implementation:

1. **Phase 1**: Core functionality and refactoring
2. **Phase 2**: Authentication flows
3. **Phase 3**: Enhanced features
4. **Phase 4**: Testing and documentation

This approach allows us to incrementally enhance the SDK while maintaining backward compatibility and focusing on the most important features first.
