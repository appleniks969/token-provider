# KMM OIDC Token Provider SDK Task List

This document tracks the status of development tasks for the OIDC Token Provider SDK.

## Implementation Tasks

| Task | Status | Notes |
|------|--------|-------|
| Create project structure | Completed | Initialized KMM project with Ktor dependencies |
| Define domain models | Completed | Created focused token-related data classes |
| Implement TokenProvider | Completed | Main entry point for the SDK |
| Implement token client | Completed | HTTP client for OIDC endpoints using Ktor |
| Implement token repository interface | Completed | Interface for token storage |
| Implement in-memory repository | Completed | For testing and development |
| Implement Android secure repository | Completed | Using Android Keystore and SharedPreferences |
| Implement iOS secure repository | Completed | Using iOS Keychain |
| Add auto_login_code support | Completed | API for requesting and storing auto login codes |
| Implement scoped repository | Completed | Support for multiple token types and scopes |
| Refine naming and architecture | Completed | Updated naming to follow best practices |

## Documentation Tasks

| Task | Status | Notes |
|------|--------|-------|
| Create README | Completed | Basic usage instructions |
| Create API reference | Completed | Detailed API documentation |
| Create architecture doc | Completed | Overview of the SDK architecture |
| Create task tracking | Completed | This document |

## Testing Tasks

| Task | Status | Notes |
|------|--------|-------|
| Unit tests for models | Pending | Test serialization, expiration logic, etc. |
| Unit tests for token client | Pending | Test API request/response handling |
| Unit tests for token provider | Pending | Test token management logic |
| Unit tests for repositories | Pending | Test storage logic |
| Integration tests | Pending | Test with mock OIDC server |
| Platform-specific tests | Pending | Test Android and iOS implementations |

## Packaging Tasks

| Task | Status | Notes |
|------|--------|-------|
| Configure Gradle for publishing | Pending | Set up Maven publishing |
| Configure CocoaPods for iOS | Pending | Set up CocoaPods integration |
| Create sample applications | Pending | Android and iOS sample apps |
| CI/CD setup | Pending | GitHub Actions or similar |

## Future Enhancements

| Task | Status | Notes |
|------|--------|-------|
| Support for password grant | Pending | Add username/password authentication |
| ID token validation | Pending | JWT validation for ID tokens |
| Support for device grant | Pending | Add device code flow |
| Improved error handling | Pending | More detailed error information |
| Offline support | Pending | Better handling of offline scenarios |
