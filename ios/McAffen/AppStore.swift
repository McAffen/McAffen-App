import Foundation
import SwiftUI
import Shared

/// Zentraler Zustand der iOS-App. Holt die Daten über den gemeinsamen
/// Kotlin-Kern (`Shared`), damit Android und iOS dieselbe API-Logik nutzen.
@MainActor
final class AppStore: ObservableObject {

    @Published var players: [PlayerRow] = []
    @Published var status: StatusView?
    @Published var loading = false
    @Published var errorText: String?
    @Published var sort: BoardSort = .waves
    @Published var playerName: String {
        didSet { UserDefaults.standard.set(playerName, forKey: Self.nameKey) }
    }
    @Published var voteBrowserInApp: Bool {
        didSet { UserDefaults.standard.set(voteBrowserInApp, forKey: Self.browserKey) }
    }
    @Published var lastVoted: [String: Date] {
        didSet { Self.saveVotes(lastVoted) }
    }
    @Published var update: UpdateBanner?

    private let api = McAffenApi()
    private static let nameKey = "mcaffen.playerName"
    private static let browserKey = "mcaffen.voteBrowserInApp"
    private static let votesKey = "mcaffen.voteTimes"

    struct PlayerRow: Identifiable {
        let id: String
        let name: String
        let score: Int
    }

    struct StatusView {
        let onlinePlayers: Int
        let networkServers: Int
        let messages: [String]
        let servers: [(name: String, type: String, online: Bool, players: Int)]
    }

    struct UpdateBanner: Identifiable {
        let id = UUID()
        let version: String
        let notes: [String]
        let url: URL?
    }

    init() {
        playerName = UserDefaults.standard.string(forKey: Self.nameKey) ?? ""
        voteBrowserInApp = UserDefaults.standard.object(forKey: Self.browserKey) as? Bool ?? true
        lastVoted = Self.loadVotes()
    }

    /// Lädt Rangliste und Status. Fehler landen sichtbar in `errorText`,
    /// statt still zu verschwinden.
    func refresh() async {
        loading = true
        errorText = nil
        defer { loading = false }

        do {
            let list = try await api.leaderboard(sort: sort)
            players = list.map { PlayerRow(id: $0.name, name: $0.name, score: Int($0.score)) }
        } catch {
            errorText = "Rangliste: \(error.localizedDescription)"
        }

        do {
            let s = try await api.status()
            status = StatusView(
                onlinePlayers: Int(s.onlinePlayers),
                networkServers: Int(s.networkServers),
                messages: s.messages,
                servers: s.servers.map { ($0.name, $0.type, $0.online, Int($0.players)) }
            )
        } catch {
            if errorText == nil { errorText = "Status: \(error.localizedDescription)" }
        }
    }

    func select(_ sort: BoardSort) async {
        self.sort = sort
        await refresh()
    }

    /// Prüft, ob im App Store bzw. auf GitHub eine neuere Version steht.
    func checkUpdate() async {
        let current = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
        guard let url = URL(string: "https://api.github.com/repos/McAffen/McAffen-App/releases/latest") else { return }
        var request = URLRequest(url: url)
        request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
        guard let (data, _) = try? await URLSession.shared.data(for: request),
              let json = String(data: data, encoding: .utf8),
              let release = UpdateLogic.shared.newerRelease(
                json: json,
                currentVersion: current,
                requireApk: false
              )
        else { return }
        update = UpdateBanner(
            version: release.version,
            notes: UpdateLogic.shared.noteLines(notes: release.notes),
            url: URL(string: release.htmlUrl)
        )
    }

    func markVoted(_ site: String) {
        lastVoted[site] = Date()
    }

    func canVote(_ site: VoteSite) -> Bool {
        guard let last = lastVoted[site.site] else { return true }
        let cooldown = Double(max(site.hours, 1)) * 3600
        return Date().timeIntervalSince(last) >= cooldown
    }

    func remaining(_ site: VoteSite) -> String? {
        guard let last = lastVoted[site.site] else { return nil }
        let cooldown = Double(max(site.hours, 1)) * 3600
        let left = cooldown - Date().timeIntervalSince(last)
        guard left > 0 else { return nil }
        let hours = Int(left) / 3600
        let minutes = (Int(left) % 3600) / 60
        return hours > 0 ? "in \(hours) h \(minutes) min" : "in \(minutes) min"
    }

    func voteUrl(_ site: VoteSite) -> URL? {
        URL(string: DefaultVoteSites.shared.urlFor(site: site, playerName: playerName))
    }

    private static func loadVotes() -> [String: Date] {
        guard let raw = UserDefaults.standard.dictionary(forKey: votesKey) as? [String: Double] else { return [:] }
        return raw.mapValues { Date(timeIntervalSince1970: $0) }
    }

    private static func saveVotes(_ votes: [String: Date]) {
        let raw = votes.mapValues { $0.timeIntervalSince1970 }
        UserDefaults.standard.set(raw, forKey: votesKey)
    }
}
