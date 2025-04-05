package com.example.oidc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the essential OIDC endpoints from the discovery document
 */
@Serializable
data class TokenEndpoints(
    @SerialName("issuer") val issuer: String,
    @SerialName("token_endpoint") val tokenEndpoint: String,
    @SerialName("jwks_uri") val jwksUri: String
)

/**
 * Represents the token response from the OIDC provider
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("scope") val scope: String? = null,
    @SerialName("auto_login_code") val autoLoginCode: String? = null
)

/**
 * Error response from the OIDC provider
 */
@Serializable
data class TokenError(
    @SerialName("error") val error: String,
    @SerialName("error_description") val errorDescription: String? = null,
    @SerialName("error_uri") val errorUri: String? = null
)

/**
 * Represents the stored token information
 */
@Serializable
data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val scope: String?,
    val expiresAt: Long,
    val autoLoginCode: String? = null
) {
    /**
     * Check if the access token is expired
     * 
     * @param timestampInSeconds Current time in seconds
     * @return true if token is expired
     */
    fun isExpired(timestampInSeconds: Long = currentTimeInSeconds()): Boolean {
        return timestampInSeconds >= expiresAt
    }

    companion object {
        /**
         * Create TokenSet from a TokenResponse with proper expiration handling
         * 
         * @param response The token response from the server
         * @param bufferTimeSeconds Time in seconds to subtract from expiration as a safety buffer
         * @param clockSkewSeconds Additional time to subtract to account for client-server clock differences
         * @return TokenSet with calculated expiration time
         */
        fun fromResponse(
            response: TokenResponse,
            bufferTimeSeconds: Int = 30,
            clockSkewSeconds: Int = 60
        ): TokenSet {
            val expiresAt = currentTimeInSeconds() + response.expiresIn - bufferTimeSeconds - clockSkewSeconds
            return TokenSet(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                tokenType = response.tokenType,
                scope = response.scope,
                expiresAt = expiresAt,
                autoLoginCode = response.autoLoginCode
            )
        }

        fun currentTimeInSeconds(): Long = System.currentTimeMillis() / 1000
    }
}