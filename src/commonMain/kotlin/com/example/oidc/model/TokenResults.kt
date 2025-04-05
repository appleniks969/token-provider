package com.example.oidc.model

/**
 * Result wrapper for token operations
 */
sealed class TokenResult<out T> {
    data class Success<T>(val data: T) : TokenResult<T>()
    data class Error(val exception: Throwable) : TokenResult<Nothing>()
}

/**
 * Token state representing the current state of tokens
 */
sealed class TokenState {
    object NoToken : TokenState()
    object Refreshing : TokenState()
    data class Valid(val tokens: TokenSet) : TokenState()
    data class Invalid(val message: String) : TokenState()
}