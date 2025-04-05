package com.example.oidc.client

import com.example.oidc.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

/**
 * Implementation of TokenClient using Ktor
 */
class KtorTokenClient(
    private val httpClient: HttpClient
) : TokenClient {
    private val _tokenState = MutableStateFlow<TokenState>(TokenState.NoToken)
    override val tokenState: Flow<TokenState> = _tokenState
    
    override suspend fun discoverConfiguration(issuerUrl: String): TokenResult<TokenEndpoints> {
        return try {
            val wellKnownUrl = if (issuerUrl.endsWith("/")) {
                "${issuerUrl}.well-known/openid-configuration"
            } else {
                "$issuerUrl/.well-known/openid-configuration"
            }
            
            val response: TokenEndpoints = httpClient.get(wellKnownUrl).body()
            TokenResult.Success(response)
        } catch (e: Exception) {
            TokenResult.Error(e)
        }
    }
    
    override suspend fun refreshToken(
        tokenEndpoint: String,
        clientId: String,
        refreshToken: String,
        clientSecret: String?,
        scope: String?
    ): TokenResult<TokenSet> {
        return try {
            _tokenState.value = TokenState.Refreshing
            
            val parameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("client_id", clientId)
                append("refresh_token", refreshToken)
                clientSecret?.let { append("client_secret", it) }
                scope?.let { append("scope", it) }
            }
            
            val response = httpClient.submitForm(
                url = tokenEndpoint,
                formParameters = parameters
            )
            
            if (response.status.isSuccess()) {
                val tokenResponse: TokenResponse = response.body()
                val tokenSet = TokenSet.fromResponse(tokenResponse)
                _tokenState.value = TokenState.Valid(tokenSet)
                TokenResult.Success(tokenSet)
            } else {
                val error: TokenError = response.body()
                _tokenState.value = TokenState.Invalid(error.errorDescription ?: error.error)
                TokenResult.Error(Exception(error.errorDescription ?: error.error))
            }
        } catch (e: Exception) {
            _tokenState.value = TokenState.Invalid(e.message ?: "Unknown error")
            TokenResult.Error(e)
        }
    }
    
    override suspend fun requestAutoLoginCode(
        tokenEndpoint: String,
        clientId: String,
        username: String,
        clientSecret: String?,
        additionalParams: Map<String, String>
    ): TokenResult<String> {
        return try {
            val parameters = Parameters.build {
                append("client_id", clientId)
                clientSecret?.let { append("client_secret", it) }
                append("username", username)
                append("grant_type", "auto_login_code")
                
                // Add any additional parameters
                additionalParams.forEach { (key, value) ->
                    append(key, value)
                }
            }
            
            val response = httpClient.submitForm(
                url = tokenEndpoint,
                formParameters = parameters
            )
            
            if (response.status.isSuccess()) {
                val tokenResponse: TokenResponse = response.body()
                tokenResponse.autoLoginCode?.let {
                    TokenResult.Success(it)
                } ?: TokenResult.Error(Exception("No auto login code returned"))
            } else {
                val error: TokenError = response.body()
                TokenResult.Error(Exception(error.errorDescription ?: error.error))
            }
        } catch (e: Exception) {
            TokenResult.Error(e)
        }
    }
    
    companion object {
        /**
         * Create a KtorTokenClient with default configuration
         * 
         * @param engine HTTP engine to use
         * @param json Custom JSON configuration (optional)
         * @return Configured KtorTokenClient
         */
        fun create(
            engine: HttpClientEngine,
            json: Json = Json { 
                ignoreUnknownKeys = true 
                isLenient = true
            }
        ): KtorTokenClient {
            val client = HttpClient(engine) {
                install(ContentNegotiation) {
                    json(json)
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 30000
                    connectTimeoutMillis = 15000
                    socketTimeoutMillis = 15000
                }
                defaultRequest {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }
            }
            
            return KtorTokenClient(client)
        }
    }
}