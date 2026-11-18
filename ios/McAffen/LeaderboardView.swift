import SwiftUI
import Shared

struct LeaderboardView: View {
    @EnvironmentObject private var store: AppStore

    var body: some View {
        NavigationStack {
            ZStack {
                Color(.systemGroupedBackground).ignoresSafeArea()
                content
            }
            .navigationTitle("Rangliste")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        Task { await store.refresh() }
                    } label: {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .refreshable { await store.refresh() }
        }
    }

    @ViewBuilder
    private var content: some View {
        VStack(spacing: 12) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(sorts, id: \.key) { item in
                        let active = item.key == store.sort.key
                        Button {
                            Task { await store.select(item) }
                        } label: {
                            Text(item.label)
                                .font(.subheadline.weight(active ? .bold : .regular))
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(active ? Theme.accent : Theme.card, in: Capsule())
                                .foregroundStyle(active ? .black : .primary)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal)
            }

            if store.loading && store.players.isEmpty {
                ProgressView("Lade Rangliste …").frame(maxHeight: .infinity)
            } else if store.players.isEmpty {
                emptyState
            } else {
                List {
                    ForEach(Array(store.players.enumerated()), id: \.element.id) { index, player in
                        HStack(spacing: 12) {
                            Text("\(index + 1)")
                                .font(.headline.monospacedDigit())
                                .frame(width: 34, alignment: .trailing)
                                .foregroundStyle(rankColor(index))
                            Text(player.name).font(.body.weight(.semibold))
                            Spacer()
                            Text(player.score.formatted())
                                .font(.callout.monospacedDigit())
                                .foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 4)
                    }
                }
                .listStyle(.plain)
            }
        }
    }

    private var sorts: [BoardSort] {
        [.waves, .kills, .games, .coins]
    }

    private func rankColor(_ index: Int) -> Color {
        switch index {
        case 0: return .yellow
        case 1: return .gray
        case 2: return .brown
        default: return .secondary
        }
    }

    private var emptyState: some View {
        VStack(spacing: 10) {
            Image(systemName: "trophy").font(.system(size: 42)).foregroundStyle(.secondary)
            Text("Rangliste ist leer").font(.headline)
            if let error = store.errorText {
                Text(error).font(.footnote).multilineTextAlignment(.center).foregroundStyle(.red)
            }
            Button("Erneut laden") { Task { await store.refresh() } }
                .buttonStyle(.borderedProminent)
                .tint(Theme.accent)
        }
        .padding(28)
        .frame(maxHeight: .infinity)
    }
}
