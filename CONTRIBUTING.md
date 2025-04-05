# Contributing to KMM OIDC Token Provider SDK

We love your input! We want to make contributing to this SDK as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Process

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

### Pull Requests

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Pull Request Process

1. Update the README.md and documentation with details of changes, if applicable
2. Update the CHANGELOG.md with details of changes
3. Increase version numbers in build.gradle.kts and any other relevant files
4. Ensure all tests pass
5. The pull request will be merged once it has been reviewed and approved

## Coding Standards

### Kotlin Coding Style

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful, descriptive names for variables, functions, and classes
- Write documentation for public APIs
- Keep functions short and focused on a single responsibility
- Use proper indentation and spacing

### Architecture Guidelines

- Follow the clean architecture principles used in the project
- Maintain separation of concerns between components
- Ensure platform-specific code is properly isolated
- Use interfaces for dependencies to maintain testability

### Testing Standards

- Write unit tests for all non-UI code
- Aim for high test coverage, especially for critical functionality
- Test both success and error paths
- Use mocks or fakes for external dependencies
- Write platform-specific tests for platform-specific implementations

## Reporting Bugs

We use GitHub issues to track public bugs. Report a bug by opening a new issue.

### Bug Report Guidelines

**Great Bug Reports** tend to have:

- A quick summary and/or background
- Steps to reproduce
  - Be specific
  - Provide sample code if possible
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

## Feature Requests

We also use GitHub issues to track feature requests. When proposing a feature:

1. Explain in detail how it would work
2. Keep the scope as narrow as possible to make it easier to implement
3. Consider the impact on existing features and backward compatibility
4. Provide code examples if possible to illustrate the feature

## License

By contributing, you agree that your contributions will be licensed under the project's license.

## Attribution

This Contributing guide is adapted from the [Briandk Contributing template](https://gist.github.com/briandk/3d2e8b3ec8daf5a27a62).