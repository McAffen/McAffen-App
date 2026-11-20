import Foundation

struct RankRow: Codable, Identifiable {
    let name: String
    let score: Int
    var id: String { name }
}

struct RawServer: Codable {
    let id: String?
    let name: String?
    let type: String?
    let version: String?
    let online: Bool
    let maintenance: Bool
    let players: Players?

    struct Players: Codable {
        let online: Int
    }

    var displayName: String { name ?? id ?? "Server" }
    var isOnline: Bool { online && !maintenance }
}

struct RawStatus: Codable {
    let servers: [RawServer]
    let messages: [String]
}

struct VoteSite: Identifiable {
    let name: String
    let url: String
    /// Manche Seiten erwarten den Spielernamen direkt am Ende der URL.
    var appendsPlayerName = false
    var id: String { name }

    func voteURL(player: String) -> URL? {
        guard appendsPlayerName else { return URL(string: url) }
        let encoded = player.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? player
        return URL(string: url + encoded)
    }
}

enum McAffenAPI {
    static let baseURL = URL(string: "https://mcaffen.de/api")!

    static func leaderboard() async throws -> [RankRow] {
        try await get(path: "rangliste", query: [URLQueryItem(name: "sort", value: "waves")])
    }

    static func status() async throws -> RawStatus {
        try await get(path: "status")
    }

    private static func get<T: Decodable>(path: String, query: [URLQueryItem] = []) async throws -> T {
        var components = URLComponents(url: baseURL.appendingPathComponent(path), resolvingAgainstBaseURL: false)!
        components.queryItems = query
        var request = URLRequest(url: components.url!)
        request.timeoutInterval = 15
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw URLError(.badServerResponse)
        }
        return try JSONDecoder().decode(T.self, from: data)
    }
}

let defaultVoteSites = [
    VoteSite(name: "minecraft-server.eu", url: "https://minecraft-server.eu/vote/index/237E8"),
    VoteSite(name: "minecraft-serverlist.net", url: "https://www.minecraft-serverlist.net/vote/60155/", appendsPlayerName: true),
    VoteSite(name: "serverliste.net", url: "https://serverliste.net/vote/5706"),
    VoteSite(name: "mc-liste.de", url: "https://www.mc-liste.de/server/358/vote#serverVotePanel"),
    VoteSite(name: "mcserv.org", url: "https://mcserv.org/de/vote/mcaffen")
]
