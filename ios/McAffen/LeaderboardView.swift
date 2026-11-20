import SwiftUI

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
            self.error = "Die Rangliste konnte nicht geladen werden."
        }
    }
}
