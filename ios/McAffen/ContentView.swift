import SwiftUI

struct ContentView: View {
    @State private var update: AvailableUpdate?

    var body: some View {
        TabView {
            LeaderboardView()
                .tabItem { Label("Rangliste", systemImage: "trophy") }
            VoteView()
                .tabItem { Label("Voten", systemImage: "checkmark.seal") }
            StatusView()
                .tabItem { Label("Status", systemImage: "antenna.radiowaves.left.and.right") }
        }
        .tint(.orange)
        .safeAreaInset(edge: .top, spacing: 0) {
            if let update {
                UpdateBanner(update: update) { self.update = nil }
            }
        }
        .task { update = await UpdateChecker.check() }
    }
}

/// Hinweis auf eine neue Version mit Link zum GitHub-Release.
struct UpdateBanner: View {
    let update: AvailableUpdate
    let dismiss: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text("Neue Version \(update.version)").font(.subheadline.bold())
                Text("Lade die neue IPA von GitHub und installiere sie über AltStore oder Sideloadly.")
                    .font(.caption)
            }
            Spacer()
            Link("GitHub", destination: update.url)
                .buttonStyle(.borderedProminent)
                .controlSize(.small)
            Button(action: dismiss) { Image(systemName: "xmark") }
                .accessibilityLabel("Schließen")
        }
        .padding(12)
        .background(.thinMaterial)
    }
}
