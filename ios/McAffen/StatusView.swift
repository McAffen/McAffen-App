import SwiftUI

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
            self.error = "Der Serverstatus konnte nicht geladen werden."
        }
    }
}
