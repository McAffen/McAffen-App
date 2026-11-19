import SafariServices
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

struct LeaderboardView: View {
    @State private var rows = [RankRow]()
    @State private var error: String?
    @State private var loading = false

    var body: some View {
        NavigationStack {
            Group {
                if loading && rows.isEmpty {
                    ProgressView("Lade Rangliste …")
                } else if let error, rows.isEmpty {
                    ErrorView(message: error, retry: load)
                } else {
                    List(Array(rows.enumerated()), id: \.element.id) { index, row in
                        HStack(spacing: 14) {
                            Text("#\(index + 1)")
                                .font(.headline.monospacedDigit())
                                .foregroundStyle(index < 3 ? .orange : .secondary)
                                .frame(width: 44, alignment: .leading)
                            Text(row.name).fontWeight(.semibold)
                            Spacer()
                            Text("\(row.score)")
                                .font(.headline.monospacedDigit())
                                .foregroundStyle(.secondary)
                        }
                    }
                    .refreshable { await loadAsync() }
                }
            }
            .navigationTitle("McAffen")
            .task { await loadAsync() }
        }
    }

    private func load() { Task { await loadAsync() } }

    private func loadAsync() async {
        loading = true
        defer { loading = false }
        do {
            rows = try await McAffenAPI.leaderboard()
            error = nil
        } catch {
            error = "Die Rangliste konnte nicht geladen werden."
        }
    }
}

struct VoteView: View {
    @AppStorage("playerName") private var playerName = ""
    @State private var selectedURL: URL?

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
                            let name = playerName.trimmingCharacters(in: .whitespacesAndNewlines)
                            let encodedName = name.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? name
                            selectedURL = URL(string: site.url + (site.name == "minecraft-serverlist.net" ? encodedName : ""))
                        } label: {
                            Label(site.name, systemImage: "arrow.up.right.square")
                        }
                        .disabled(playerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
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

struct StatusView: View {
    @State private var status: RawStatus?
    @State private var error: String?

    var body: some View {
        NavigationStack {
            Group {
                if let status {
                    List {
                        Section {
                            LabeledContent("Spieler online", value: "\(status.servers.filter(\.isOnline).reduce(0) { $0 + ($1.players?.online ?? 0) })")
                            LabeledContent("Server", value: "\(status.servers.count)")
                        }
                        Section("Server") {
                            ForEach(status.servers, id: \.displayName) { server in
                                HStack {
                                    Circle()
                                        .fill(server.isOnline ? .green : .red)
                                        .frame(width: 9, height: 9)
                                    Text(server.displayName)
                                    Spacer()
                                    Text("\(server.players?.online ?? 0)")
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                        if !status.messages.isEmpty {
                            Section("Meldungen") {
                                ForEach(status.messages, id: \.self) { Text($0) }
                            }
                        }
                    }
                    .refreshable { await load() }
                } else if let error {
                    ErrorView(message: error) { Task { await load() } }
                } else {
                    ProgressView("Lade Status …")
                }
            }
            .navigationTitle("Serverstatus")
            .task { await load() }
        }
    }

    private func load() async {
        do {
            status = try await McAffenAPI.status()
            error = nil
        } catch {
            error = "Der Serverstatus konnte nicht geladen werden."
        }
    }
}

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
