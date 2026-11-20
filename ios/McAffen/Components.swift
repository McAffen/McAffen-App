import SafariServices
import SwiftUI

struct ErrorView: View {
    let message: String
    var retry: (() -> Void)?

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "wifi.exclamationmark")
                .font(.largeTitle)
            Text(message).multilineTextAlignment(.center)
            if let retry {
                Button("Erneut versuchen", action: retry)
                    .buttonStyle(.borderedProminent)
            }
        }
        .padding()
    }
}

struct SafariView: UIViewControllerRepresentable {
    let url: URL
    func makeUIViewController(context: Context) -> SFSafariViewController { SFSafariViewController(url: url) }
    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}
