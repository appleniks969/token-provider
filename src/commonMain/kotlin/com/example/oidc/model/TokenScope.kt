package com.example.oidc.model

import kotlinx.serialization.Serializable

/**
 * Scope identifier for token storage
 */
@Serializable
data class TokenScope(
    val clientId: String,
    val userId: String? = null,
    val scope: String? = null,
    val resource: String? = null,
    val purpose: String? = null
) {
    /**
     * Convert to a storage key
     */
    fun toStorageKey(): String {
        return buildString {
            append("client_")
            append(clientId)
            
            userId?.let {
                append("_user_")
                append(it)
            }
            
            scope?.let {
                append("_scope_")
                append(it.replace(" ", "_"))
            }
            
            resource?.let {
                append("_resource_")
                append(it)
            }
            
            purpose?.let {
                append("_purpose_")
                append(it)
            }
        }
    }
    
    companion object {
        val DEFAULT = TokenScope(clientId = "default")
        
        fun forClient(clientId: String): TokenScope = TokenScope(clientId = clientId)
        
        fun forUser(clientId: String, userId: String): TokenScope = 
            TokenScope(clientId = clientId, userId = userId)
        
        fun forScope(clientId: String, scope: String, userId: String? = null): TokenScope =
            TokenScope(clientId = clientId, userId = userId, scope = scope)
        
        fun forResource(clientId: String, resource: String, userId: String? = null): TokenScope =
            TokenScope(clientId = clientId, userId = userId, resource = resource)
    }
}