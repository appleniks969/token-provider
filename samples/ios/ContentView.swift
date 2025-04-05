import SwiftUI

struct ContentView: View {
    @ObservedObject var viewModel = TokenProviderViewModel()
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                statusSection
                tokenSection
                
                Divider()
                
                buttonsSection
                
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .foregroundColor(.red)
                        .padding()
                        .multilineTextAlignment(.center)
                }
                
                Spacer()
            }
            .padding()
            .navigationTitle("OIDC Token Provider")
            .overlay(loadingOverlay)
        }
    }
    
    private var statusSection: some View {
        VStack(alignment: .leading) {
            Text("Status:")
                .font(.headline)
            Text(viewModel.status)
                .font(.subheadline)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    
    private var tokenSection: some View {
        VStack(alignment: .leading) {
            Text("Access Token:")
                .font(.headline)
            if let token = viewModel.accessToken {
                Text(token)
                    .font(.system(.subheadline, design: .monospaced))
                    .lineLimit(1)
                    .truncationMode(.tail)
            } else {
                Text("No token available")
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    
    private var buttonsSection: some View {
        VStack(spacing: 12) {
            Button("Initialize Provider") {
                viewModel.discoverConfiguration()
            }
            .buttonStyle(PrimaryButtonStyle())
            
            Button("Get Access Token") {
                viewModel.getAccessToken()
            }
            .buttonStyle(PrimaryButtonStyle())
            
            Button("Get Auto Login Code") {
                viewModel.getAutoLoginCode()
            }
            .buttonStyle(PrimaryButtonStyle())
        }
    }
    
    private var loadingOverlay: some View {
        Group {
            if viewModel.isLoading {
                ZStack {
                    Color.black.opacity(0.4)
                        .edgesIgnoringSafeArea(.all)
                    
                    VStack {
                        ProgressView()
                            .scaleEffect(1.5)
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        
                        Text("Loading...")
                            .foregroundColor(.white)
                            .padding(.top, 10)
                    }
                    .padding(20)
                    .background(Color.gray.opacity(0.7))
                    .cornerRadius(10)
                }
            }
        }
    }
}

struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color.blue.opacity(configuration.isPressed ? 0.7 : 1))
            .foregroundColor(.white)
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.98 : 1)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}