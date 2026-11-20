import SafariServices
import SwiftUI

struct VoteView: View {
    @AppStorage("playerName") private var playerName = ""
    @State private var selectedURL: URL?

    private var trimmedName: String {
        playerName.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var body: some View {
        NavigationStack {
            List {
                Section("Spielername") {
                    TextField("Minecraft-Name", text: $playerName)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                }
                Section("Vote-Seiten") {
                    ForEach(defaultVoteSites) { site in
                        Button {
                            selectedURL = site.voteURL(player: trimmedName)
                        } label: {
                            Label(site.name, systemImage: "arrow.up.right.square")
                        }
                        .disabled(trimmedName.isEmpty)
                    }
                }
            }
            .navigationTitle("Voten")
            .sheet(isPresented: Binding(
                get: { selectedURL != nil },
                set: { if !$0 { selectedURL = nil } }
            )) {
                if let selectedURL {
                    SafariView(url: selectedURL)
                }
            }
        }
    }
}
