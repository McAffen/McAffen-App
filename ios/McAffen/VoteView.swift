import SwiftUI
import SafariServices
import Shared

struct VoteView: View {
    @EnvironmentObject private var store: AppStore
    @State private var safariUrl: URL?

    var body: some View {
        NavigationStack {
            List {
                Section {
                    TextField("Dein Minecraft-Name", text: $store.playerName)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                } header: {
                    Text("Spielername")
                } footer: {
                    Text("Der Name wird in die Vote-Links eingesetzt, damit die Seite deinen Vote zuordnen kann.")
                }

                Section("Vote-Seiten") {
                    ForEach(DefaultVoteSites.shared.all.map { $0 as VoteSite }, id: \.site) { site in
                        voteRow(site)
                    }
                }
            }
            .navigationTitle("Voten")
            .sheet(item: $safariUrl) { url in
                SafariView(url: url).ignoresSafeArea()
            }
        }
    }

    @ViewBuilder
    private func voteRow(_ site: VoteSite) -> some View {
        let ready = store.canVote(site) && !store.playerName.trimmingCharacters(in: .whitespaces).isEmpty
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(site.site).font(.body.weight(.semibold))
                Spacer()
                if let remaining = store.remaining(site) {
                    Text(remaining).font(.caption).foregroundStyle(.secondary)
                }
            }
            if let reward = site.reward {
                Text(reward).font(.caption).foregroundStyle(.secondary)
            }
            Button {
                guard let url = store.voteUrl(site) else { return }
                store.markVoted(site.site)
                if store.voteBrowserInApp {
                    safariUrl = url
                } else {
                    UIApplication.shared.open(url)
                }
            } label: {
                Text(ready ? "Jetzt voten" : "Wartezeit läuft")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(Theme.accent)
            .disabled(!ready)
        }
        .padding(.vertical, 4)
    }
}

/// Eingebauter Browser für Vote-Seiten (SFSafariViewController).
struct SafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        SFSafariViewController(url: url)
    }

    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}

extension URL: @retroactive Identifiable {
    public var id: String { absoluteString }
}
