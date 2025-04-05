# OIDC Concepts Reference

This document provides information about key concepts in OpenID Connect (OIDC) that are relevant to the KMM OIDC Token Provider SDK.

## OAuth 2.0 vs OpenID Connect

**OAuth 2.0** is an authorization framework that enables third-party applications to obtain limited access to a service on behalf of a user or on its own behalf.

**OpenID Connect (OIDC)** is an identity layer built on top of OAuth 2.0. It adds authentication capabilities and standardizes many things that were left up to implementers in OAuth 2.0.

## Key OIDC Components

### Token Types

- **Access Token**: A credential used to access protected resources. It's usually short-lived.
- **Refresh Token**: A credential used to obtain new access tokens when they expire. It's usually long-lived.
- **ID Token**: A JSON Web Token (JWT) that contains claims about the authentication of an end-user.

### Endpoints

- **Authorization Endpoint**: Where the user is redirected to authenticate and authorize the application.
- **Token Endpoint**: Where applications can exchange authorization codes for tokens or refresh tokens for new access tokens.
- **UserInfo Endpoint**: Where applications can get more information about the user with a valid access token.
- **Revocation Endpoint**: Where tokens can be revoked before they expire.
- **Discovery Document Endpoint (.well-known/openid-configuration)**: Where OIDC provider metadata can be found.

### Grant Types

- **Authorization Code**: The most common flow for web and mobile applications.
- **Client Credentials**: Used for server-to-server authentication.
- **Refresh Token**: Used to obtain new access tokens.
- **Password**: A legacy grant type where the client collects the user's credentials (discouraged).
- **Implicit**: A legacy grant type for client-side applications (discouraged).

## The .well-known/openid-configuration Endpoint

### What Is It?
The discovery endpoint (/.well-known/openid-configuration) is a standardized URL that provides a JSON document containing all the necessary configuration information about an OIDC provider.

### Why It's Important
- **Dynamic Configuration**: Applications can discover the provider's endpoints and capabilities at runtime.
- **Reduced Hardcoding**: No need to hardcode URLs and supported features.
- **Provider Changes**: If the provider changes their endpoints, applications continue to work without modification.

### What It Contains
- Issuer identifier
- Authorization endpoint URL
- Token endpoint URL
- UserInfo endpoint URL
- JWKS (JSON Web Key Set) URL
- Registration endpoint URL
- Scopes and claims supported
- Response types supported
- Grant types supported
- Token endpoint auth methods supported
- And many more configuration details

### Example
```json
{
  "issuer": "https://example.auth0.com",
  "authorization_endpoint": "https://example.auth0.com/authorize",
  "token_endpoint": "https://example.auth0.com/oauth/token",
  "userinfo_endpoint": "https://example.auth0.com/userinfo",
  "jwks_uri": "https://example.auth0.com/.well-known/jwks.json",
  "response_types_supported": ["code", "token", "id_token"],
  "subject_types_supported": ["public"],
  "id_token_signing_alg_values_supported": ["RS256"]
}
```

## Auto Login Codes

Auto login codes are a less standard feature that some OIDC providers offer. They allow for simplified login experiences, especially for multi-device scenarios.

### Key Characteristics
- Typically short-lived
- Can be used once
- Allow logging in without re-entering credentials

### Common Uses
- Logging in on a different device or app
- Passwordless login
- Cross-device authentication

## Token Refresh Process

When an access token expires, the application can use a refresh token to obtain a new access token without user intervention.

### Standard Process
1. Application detects expired/expiring access token
2. Application sends refresh token to token endpoint
3. Provider validates refresh token
4. Provider issues new access token (and optionally a new refresh token)
5. Application continues using the new access token

### Security Considerations
- Using buffer time before actual expiration (our SDK uses 30 seconds)
- Accounting for clock skew between client and server (our SDK uses 60 seconds)
- Secure storage of refresh tokens
- Handling of token rotation (some providers issue new refresh tokens with each refresh)

## Secure Token Storage

Tokens should be stored securely to prevent theft and unauthorized use.

### Android Security
- **Android Keystore System**: Hardware-backed secure storage for cryptographic keys
- **AES-GCM Encryption**: Authenticated encryption for token data
- **SharedPreferences**: For storing encrypted data with private mode

### iOS Security
- **Keychain Services**: Apple's secure storage mechanism
- **Access Control**: Restricted access to stored items
- **Device Locking**: Can tie access to device unlock state

## Scoped Token Management

Some applications need to manage multiple sets of tokens for different purposes.

### Common Scenarios
- Different scopes (read vs. write)
- Different resources (API 1 vs. API 2)
- Different users on the same device
- Different client IDs

### Implementation Approaches
- Scope-based keys for storage
- Hierarchical organization
- Metadata for tracking and management