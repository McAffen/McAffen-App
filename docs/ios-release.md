# iOS: Bauen und Verteilen

Die iOS-App (`ios/`, SwiftUI) und der gemeinsame Kotlin-Kern (`shared/`) werden vom
Workflow `.github/workflows/release.yml` auf einem GitHub-macOS-Runner gebaut. Dafür
braucht niemand einen eigenen Mac.

## Was der Workflow erzeugt

| Datei im Release | Zweck |
|---|---|
| `McAffen-unsigned.ipa` | Unsigniert, für echte Geräte. Installation per AltStore/Sideloadly (siehe [README](../README.md#iphone--ipad)) |
| `McAffen-iOS-Simulator.app.zip` | Nur für den Xcode-Simulator |

Zusätzlich liegen `Shared.xcframework` und die Artefakte 14 Tage am Workflow-Lauf.
Der Schritt für die IPA ist als `continue-on-error` markiert: Scheitert er, bleibt das
Release trotzdem möglich, nur ohne IPA.

## Lokal bauen (braucht einen Mac)

```bash
brew install xcodegen
cd ios && xcodegen generate
open McAffen.xcodeproj
```

Zum Testen auf dem eigenen Gerät in Xcode unter *Signing* das persönliche Team wählen.

## Eigener Mac als Runner (optional)

Im Job `ios` von `release.yml` `runs-on: macos-15` durch `[self-hosted, macOS]` ersetzen
und den Mac unter *Settings → Actions → Runners → New self-hosted runner* registrieren.
Der Mac braucht Xcode, JDK 21 und `brew install xcodegen`.

## Offizielle Verteilung (noch offen)

Für TestFlight oder den App Store ist zwingend nötig:

- Apple Developer Program (99 € pro Jahr)
- App-Icon (1024×1024 PNG ohne Transparenz) in `ios/McAffen/Assets.xcassets`, Screenshots, Datenschutz-URL
- Bundle-ID `de.mcaffen.app` bei Apple registrieren
- Signing-Secrets im Repo (`.p12`, Provisioning-Profil, App-Store-Connect-API-Key) und ein Upload-Job im Workflow

Bis dahin ist der Weg über die unsignierte IPA der einfachste.

## Unterschiede zu Android

- Updates laufen später über den App Store bzw. TestFlight, nicht über einen In-App-Download.
- Die iOS-App hat weder Offline-Cache noch Einstellungen, LAN-Brücke oder Sprachwahl.
- Vote-Seiten öffnen sich im eingebauten Safari-Fenster.
