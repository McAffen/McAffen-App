import SwiftUI
import Shared

struct StatusViewScreen: View {
    @EnvironmentObject private var store: AppStore

    var body: some View {
        NavigationStack {
            List {
                if let status = store.status {
                    Section("Netzwerk") {
                        LabeledContent("Spieler online", value: "\(status.onlinePlayers)")
                        LabeledContent("Server", value: "\(status.networkServers)")
                    }
                    Section("Server") {
                        ForEach(status.servers, id: \.name) { server in
                            HStack {
                                Circle()
                                    .fill(server.online ? Theme.accent : .red)
                                    .frame(width: 8, height: 8)
                                VStack(alignment: .leading) {
                                    Text(server.name).font(.body.weight(.semibold))
                                    Text(server.type).font(.caption).foregroundStyle(.secondary)
                                }
                                Spacer()
                                Text("\(server.players)").monospacedDigit().foregroundStyle(.secondary)
                            }
                        }
                    }
                    if !status.messages.isEmpty {
                        Section("Meldungen") {
                            ForEach(status.messages, id: \.self) { message in
                                Text(message).font(.footnote)
                            }
                        }
                    }
                } else {
                    ProgressView("Lade Status …")
                }

                Section("LAN-Welt auf der Konsole") {
                    LabeledContent("Bedrock-Adresse", value: LanInfo.shared.BEDROCK_ADDRESS)
                    ForEach(LanInfo.shared.steps(localIp: nil).map { $0 as String }, id: \.self) { step in
                        Text(step).font(.footnote).foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Status")
            .refreshable { await store.refresh() }
        }
    }
}
