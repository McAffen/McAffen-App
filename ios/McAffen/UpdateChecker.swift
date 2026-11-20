import Foundation

/// Ein neueres GitHub-Release als die installierte App.
struct AvailableUpdate: Equatable {
    let version: String
    let url: URL
}

/// Prüft, ob es auf GitHub ein neueres Release gibt. iOS erlaubt keine
/// Selbst-Installation; die App zeigt nur einen Hinweis mit Link zum Download.
enum UpdateChecker {
    private struct Release: Decodable {
        let tagName: String
        let htmlUrl: URL
        let draft: Bool
        let prerelease: Bool

        enum CodingKeys: String, CodingKey {
            case tagName = "tag_name"
            case htmlUrl = "html_url"
            case draft
            case prerelease
        }
    }

    private static let latestURL = URL(string: "https://api.github.com/repos/McAffen/McAffen-App/releases/latest")!

    static var installedVersion: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "0"
    }

    /// Liefert das Update oder `nil` (aktuell, offline oder Fehler).
    static func check() async -> AvailableUpdate? {
        var request = URLRequest(url: latestURL)
        request.timeoutInterval = 10
        request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
        guard
            let (data, response) = try? await URLSession.shared.data(for: request),
            (response as? HTTPURLResponse)?.statusCode == 200,
            let release = try? JSONDecoder().decode(Release.self, from: data),
            !release.draft, !release.prerelease
        else { return nil }
        let version = release.tagName.hasPrefix("v") ? String(release.tagName.dropFirst()) : release.tagName
        guard isNewer(version, than: installedVersion) else { return nil }
        return AvailableUpdate(version: version, url: release.htmlUrl)
    }

    /// Vergleicht Versionen wie `1.0.12` Stelle für Stelle.
    static func isNewer(_ candidate: String, than current: String) -> Bool {
        let a = candidate.split(separator: ".").map { Int($0) ?? 0 }
        let b = current.split(separator: ".").map { Int($0) ?? 0 }
        for index in 0..<max(a.count, b.count) {
            let x = index < a.count ? a[index] : 0
            let y = index < b.count ? b[index] : 0
            if x != y { return x > y }
        }
        return false
    }
}
