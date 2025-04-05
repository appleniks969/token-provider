# Project Structure

This document describes the directory structure and organization of the KMM OIDC Token Provider SDK.

## Directory Structure

```
Kmm-Oidc/
├── docs/                       # Documentation
│   ├── API.md                  # API reference
│   ├── ARCHITECTURE.md         # Architecture details
│   ├── PROJECT_STRUCTURE.md    # This file
│   └── TASKS.md                # Task tracking
├── memory-bank/                # Key concepts and implementation notes
│   ├── CONCEPTS.md             # OIDC concepts reference
│   ├── DESIGN_DECISIONS.md     # Design decision documentation
│   └── IMPLEMENTATION_NOTES.md # Implementation details
├── samples/                    # Sample usage
│   ├── android/                # Android sample
│   └── ios/                    # iOS sample
├── src/                        # Source code
│   ├── androidMain/            # Android-specific code
│   │   └── kotlin/
│   │       └── com/example/oidc/
│   │           ├── examples/   # Android usage examples
│   │           └── storage/    # Android storage implementation
│   ├── commonMain/             # Shared code
│   │   └── kotlin/
│   │       └── com/example/oidc/
│   │           ├── client/     # Token client interface and implementation
│   │           ├── examples/   # Common usage examples
│   │           ├── model/      # Domain models
│   │           ├── storage/    # Storage interfaces
│   │           └── TokenProvider.kt # Main entry point
│   ├── commonTest/             # Shared tests
│   │   └── kotlin/
│   │       └── com/example/oidc/
│   │           └── ...         # Test classes
│   └── iosMain/                # iOS-specific code
│       └── kotlin/
│           └── com/example/oidc/
│               ├── examples/   # iOS usage examples
│               └── storage/    # iOS storage implementation
├── .gitignore                  # Git ignore file
├── build.gradle.kts            # Gradle build configuration
├── CHANGELOG.md                # Version history
├── CONTRIBUTING.md             # Contribution guidelines
├── README.md                   # Main README
└── settings.gradle.kts         # Gradle settings
```

## Key Components

### Source Code Organization

The source code is organized according to the Kotlin Multiplatform conventions:

- **commonMain**: Contains all the shared code that works on both platforms
  - **model**: Domain models like TokenSet, TokenEndpoints, etc.
  - **client**: Interfaces and implementations for the token client
  - **storage**: Interfaces for token storage
  - **TokenProvider.kt**: The main entry point and API surface

- **androidMain**: Contains Android-specific implementations
  - **storage**: Android-specific secure storage implementation

- **iosMain**: Contains iOS-specific implementations
  - **storage**: iOS-specific secure storage implementation

### Testing

The project includes tests for the shared code:

- **commonTest**: Tests for shared components
  - Unit tests for models, providers, and other shared logic

### Documentation

The project includes comprehensive documentation:

- **docs/**: Technical documentation about the API, architecture, etc.
- **memory-bank/**: Conceptual documentation, design decisions, etc.
- **samples/**: Sample code showing how to use the SDK

## Dependencies

The project relies on the following key dependencies:

- **Kotlin Multiplatform**: For cross-platform development
- **Kotlinx Serialization**: For JSON serialization/deserialization
- **Ktor**: For HTTP client functionality
- **Kotlinx Coroutines**: For asynchronous programming

## Gradle Configuration

The project uses the Kotlin Multiplatform Gradle plugin along with the Android Library plugin. The key configuration files are:

- **build.gradle.kts**: Main build configuration
- **settings.gradle.kts**: Project settings