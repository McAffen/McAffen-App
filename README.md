# McAffen App

Die App zu [mcaffen.de](https://mcaffen.de): Rangliste, Voten und Serverstatus – für **Android** und **iOS**.

## Installation

Alle Dateien gibt es unter [**Releases**](https://github.com/McAffen/McAffen-App/releases/latest).

### Android

1. Auf dem Handy die Datei `McAffen-release.apk` aus dem neuesten Release herunterladen.
2. Öffnen und die Installation erlauben („Aus dieser Quelle installieren“ bestätigen).
3. Updates lädt die App danach selbst herunter und installiert sie über die bestehende Version.

Meldet Play Protect eine Warnung, hilft [docs/play-protect.md](docs/play-protect.md).

### iPhone / iPad

Apple erlaubt keine Installation einer Datei per Klick. Es gibt zwei Wege:

- **Mit AltStore oder Sideloadly (kostenlos, braucht einen PC/Mac):**
  1. `McAffen-unsigned.ipa` aus dem Release herunterladen.
  2. [AltStore](https://altstore.io) oder [Sideloadly](https://sideloadly.io) auf dem Computer installieren, das iPhone per Kabel verbinden.
  3. Die IPA in das Programm ziehen und mit der eigenen Apple-ID anmelden. Es erledigt das Signieren.
  4. Am iPhone unter *Einstellungen → Allgemein → VPN & Geräteverwaltung* dem eigenen Profil vertrauen.

  **Updates:** Die iOS-App zeigt oben einen Hinweis, sobald es eine neuere Version gibt, und verlinkt direkt auf das GitHub-Release. Die neue IPA wie oben installieren. Sie ersetzt die alte App, löschen ist nicht nötig (dieselbe Apple-ID verwenden, damit Daten und Einstellungen erhalten bleiben).

  Mit einer kostenlosen Apple-ID läuft die App 7 Tage und muss dann neu signiert werden (AltStore macht das automatisch, solange der Computer im selben WLAN ist).
- **TestFlight / App Store:** noch nicht verfügbar, siehe [docs/ios-release.md](docs/ios-release.md).

`McAffen-iOS-Simulator.app.zip` im Release ist nur für Entwickler: Sie läuft im Xcode-Simulator, nicht auf einem iPhone.

## Funktionen

- **Sprachen:** Deutsch und Englisch. Beim ersten Start wählst du die Sprache, später unter *Einstellungen → Sprache*. Die Texte der API (Meldungen, News, Spielmodi) folgen der Sprache.
- **Rangliste:** Platzierungen aller Spieler, für *Nacht der Untoten* und *Jump'n'Run*.
- **Voten:** Vote-Links werden erst nach erfolgreicher Spielersuche und HTTPS-Prüfung freigeschaltet.
- **Serverstatus:** Spielerzahl, Server und Meldungen.
- **Android zusätzlich:** Offline-Cache, Bedrock-LAN-Brücke für Konsolen, In-App-Updates, Themes. Die Rangliste zeigt auch Jump'n'Run.

Die iOS-App ist eine schlanke SwiftUI-Version mit Rangliste, Voten und Status (aktuell nur Deutsch).

## Projektaufbau

| Ordner | Inhalt |
|---|---|
| `app/` | Android-App (Jetpack Compose, Material 3, Room-Cache, Retrofit/Moshi) |
| `ios/` | iPhone-App (SwiftUI), Xcode-Projekt wird mit XcodeGen aus `project.yml` erzeugt |
| `shared/` | Gemeinsamer Kotlin-Kern: Modelle, API-Client (Ktor), Vote-Seiten, Update-Logik |
| `docs/` | Anleitungen: [iOS](docs/ios-release.md), [Signing](docs/release-signing.md), [Play Protect](docs/play-protect.md), [Issue-Vorlagen](docs/issues) |
| `api-spec.yaml` | Beschreibung der mcaffen.de-API |

Die App speichert keine Zugangsdaten und protokolliert weder Netzwerk-URLs noch Spielernamen.

## Entwickeln

```bash
# Android: Tests, Lint, Debug-APK
./gradlew test lint assembleDebug

# iOS (nur auf einem Mac)
brew install xcodegen
cd ios && xcodegen generate && open McAffen.xcodeproj
```

## Releases und CI

- **Pull Requests und `main`:** Der Workflow *Android CI* baut, testet und lintet.
- **Push auf `main`:** *Cross-platform Release* baut zuerst iOS (macOS-Runner), danach die signierte Android-APK und veröffentlicht alles als GitHub-Release (`v1.0.N`). Schlägt der iOS-Build fehl, gibt es kein Release.
- **Android-Signing:** Für Updates über die installierte App muss immer derselbe Schlüssel verwendet werden. Die GitHub-Secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` und `RELEASE_KEY_PASSWORD` einmalig hinterlegen, siehe [docs/release-signing.md](docs/release-signing.md).
