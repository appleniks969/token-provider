import Foundation
import OidcTokenProvider
import Combine

class TokenProviderViewModel: ObservableObject {
    
    // MARK: - Published properties
    
    @Published var status: String = "Not initialized"
    @Published var accessToken: String? = nil
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    
    // MARK: - Properties
    
    private let tokenProvider: TokenProvider
    private var cancellables = Set<AnyCancellable>()
    
    // OIDC Configuration
    private let issuerUrl = "https://example.auth0.com"
    private let clientId = "your-client-id" // Replace with your client ID
    private let clientSecret = "your-client-secret" // Replace with your client secret or nil
    
    // MARK: - Initialization
    
    init() {
        // Create TokenProvider using the simplified factory method
        tokenProvider = IOSExampleKt.createTokenProvider(
            issuerUrl: issuerUrl,
            clientId: clientId,
            clientSecret: clientSecret
        )
        
        // Observe token state changes
        observeTokenState()
    }
    
    // MARK: - Public methods
    
    func getAccessToken() {
        isLoading = true
        status = "Getting access token..."
        
        let completionHandler = { (result: TokenResult<TokenSet>) -> Void in
            DispatchQueue.main.async {
                self.isLoading = false
                
                switch result {
                case is TokenResult.Success<TokenSet>:
                    let success = result as! TokenResult.Success<TokenSet>
                    let tokens = success.data
                    self.status = "Access token obtained"
                    self.accessToken = String(tokens.accessToken.prefix(10)) + "..."
                    self.errorMessage = nil
                    print("Got access token: \(tokens.accessToken)")
                    
                case is TokenResult.Error:
                    let error = result as! TokenResult.Error
                    self.status = "Failed to get access token"
                    self.errorMessage = error.exception.message ?? "Unknown error"
                    print("Access token error: \(error.exception)")
                    
                default:
                    self.status = "Unknown result type"
                }
            }
        }
        
        // Call the SDK - discovery is handled automatically
        TokenProviderKt.getAccessToken(
            self.tokenProvider, 
            forceRefresh: false,
            completionHandler: completionHandler
        )
    }
    
    func getAutoLoginCode() {
        isLoading = true
        status = "Getting auto login code..."
        
        // First check if we already have one
        TokenProviderKt.getAutoLoginCode(self.tokenProvider) { (existingCode: NSString?) in
            if let code = existingCode {
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.status = "Found existing auto login code"
                    self.errorMessage = nil
                    print("Found existing auto login code: \(code)")
                    
                    // Show alert with code
                    // In a real app, you'd use UIKit/SwiftUI alerts
                    print("Auto login code: \(code)")
                }
                return
            }
            
            // Request a new code
            let completionHandler = { (result: TokenResult<NSString>) -> Void in
                DispatchQueue.main.async {
                    self.isLoading = false
                    
                    switch result {
                    case is TokenResult.Success<NSString>:
                        let success = result as! TokenResult.Success<NSString>
                        let code = success.data as String
                        self.status = "Auto login code obtained"
                        self.errorMessage = nil
                        print("Got auto login code: \(code)")
                        
                        // Show alert with code
                        // In a real app, you'd use UIKit/SwiftUI alerts
                        print("Auto login code: \(code)")
                        
                    case is TokenResult.Error:
                        let error = result as! TokenResult.Error
                        self.status = "Failed to get auto login code"
                        self.errorMessage = error.exception.message ?? "Unknown error"
                        print("Auto login code error: \(error.exception)")
                        
                    default:
                        self.status = "Unknown result type"
                    }
                }
            }
            
            // Call the SDK - no need to worry about discovery
            TokenProviderKt.requestAutoLoginCode(
                self.tokenProvider,
                username: "user@example.com", // Replace with actual username
                additionalParams: ["redirect_uri": "myapp://callback"],
                completionHandler: completionHandler
            )
        }
    }
    
    // MARK: - Private methods
    
    private func observeTokenState() {
        // In a real app, you'd observe the Kotlin Flow
        // Here's a simplified version that would need to be implemented 
        // with a proper Kotlin/Native interop mechanism
        
        // Example using a hypothetical extension (not actual code)
        // tokenProvider.tokenState.watch { state in
        //     DispatchQueue.main.async {
        //         switch state {
        //         case is TokenState.NoToken:
        //             self.status = "No tokens available"
        //         case is TokenState.Refreshing:
        //             self.status = "Refreshing tokens..."
        //         case is TokenState.Valid:
        //             let validState = state as! TokenState.Valid
        //             let tokens = validState.tokens
        //             self.status = "Token valid until: \(tokens.expiresAt)"
        //         case is TokenState.Invalid:
        //             let invalidState = state as! TokenState.Invalid
        //             self.status = "Token error: \(invalidState.message)"
        //         default:
        //             self.status = "Unknown state"
        //         }
        //     }
        // }
    }
}