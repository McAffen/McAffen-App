import SwiftUI
import Shared

struct RootView: View {
    @EnvironmentObject private var store: AppStore
    @State private var tab: Tab = .board

    enum Tab: Hashable { case board, vote, status, settings }

    var body: some View {
        TabView(selection: $tab) {
            LeaderboardView()
                .tabItem { Label("Rangliste", systemImage: "trophy.fill") }
                .tag(Tab.board)
            VoteView()
                .tabItem { Label("Voten", systemImage: "hand.thumbsup.fill") }
                .tag(Tab.vote)
            StatusViewScreen()
                .tabItem { Label("Status", systemImage: "server.rack") }
                .tag(Tab.status)
            SettingsView()
                .tabItem { Label("Einstellungen", systemImage: "gearshape.fill") }
                .tag(Tab.settings)
        }
        .tint(Theme.accent)
        .alert(item: $store.update) { banner in
            Alert(
                title: Text("Update verfügbar: \(banner.version)"),
                message: Text(banner.notes.prefix(6).joined(separator: "\n")),
                primaryButton: .default(Text("Zum App Store")) {
                    if let url = banner.url { UIApplication.shared.open(url) }
                },
                secondaryButton: .cancel(Text("Später"))
            )
        }
        .task { await store.checkUpdate() }
    }
}

enum Theme {
    static let accent = Color(red: 0.13, green: 0.77, blue: 0.37)
    static let card = Color(.secondarySystemBackground)
}
