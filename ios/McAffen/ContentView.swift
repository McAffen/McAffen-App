import SwiftUI

struct ContentView: View {
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
    }
}
