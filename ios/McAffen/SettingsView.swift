import SwiftUI
import Shared

struct SettingsView: View {
    @EnvironmentObject private var store: AppStore

    private var version: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "–"
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Vote-Links") {
                    Picker("Öffnen in", selection: $store.voteBrowserInApp) {
                        Text("In der App").tag(true)
                        Text("Safari").tag(false)
                    }
                    .pickerStyle(.segmented)
                    Text("„In der App“ öffnet die Vote-Seite im eingebauten Browser – zurück geht es mit „Fertig“.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }

                Section("Update") {
                    LabeledContent("Installiert", value: version)
                    Button("Nach Update suchen") {
                        Task { await store.checkUpdate() }
                    }
                }

                Section("Über") {
                    LabeledContent("Server", value: "mcaffen.de")
                    Link("GitHub-Repository", destination: URL(string: "https://github.com/McAffen/McAffen-App")!)
                }
            }
            .navigationTitle("Einstellungen")
        }
    }
}
