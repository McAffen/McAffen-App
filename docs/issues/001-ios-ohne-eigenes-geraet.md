# [Feature] iOS-Unterstützung ohne eigenes iOS-Gerät (CI-Build, Simulator-Tests, TestFlight)

**Labels:** `enhancement`, `ios`, `ci`

## Ausgangslage

- Der gemeinsame Kern (`shared/`) hat seit #17 die Targets `iosArm64` und `iosSimulatorArm64`,
  baut ein `Shared.xcframework` und läuft im iOS-Simulator auf einem GitHub-macOS-Runner
  (`.github/workflows/ios.yml`). Ohne eigenen Mac und ohne iPhone.
- Die iOS-Oberfläche fehlt: Es gibt keinen Ordner `ios/`. `docs/ios-release.md` beschreibt
  sie als Ziel, nicht als Bestand.
- Der Maintainer besitzt weder ein iPhone noch einen Mac.

## Ziel

Die iOS-App bauen, testen und verteilen, ohne selbst ein Apple-Gerät zu besitzen.
Alles läuft über GitHub Actions (macOS-Runner), Simulator und TestFlight-Tester.

## Vorschlag

### 1. Oberfläche: SwiftUI oder Compose Multiplatform (Entscheidung offen)

- **A) SwiftUI in `ios/` mit XcodeGen.** Native Optik, wie in `docs/ios-release.md` geplant.
  Nachteil: ohne Mac und ohne Simulator-Sicht schwer blind zu schreiben.
- **B) Compose Multiplatform.** Die bestehende Compose-Oberfläche wird wiederverwendet.
  Dafür müssen Android-Teile (Room, WorkManager, DownloadManager, `LanBridge`) hinter
  `expect/actual` oder in plattformspezifische Module wandern.
- Empfehlung: **A**, weil `shared/` bereits die Daten liefert und SwiftUI ohne
  Umbau der Android-Architektur auskommt. Screenshots aus dem Simulator (Schritt 3)
  ersetzen den Blick aufs echte Gerät.

### 2. CI: `ios.yml` ist vorhanden, wird ausgebaut

- [x] Runner `macos-15` (die in der Doku genannte `macos-13` ist veraltet)
- [x] `iosSimulatorArm64Test` und `assembleSharedXCFramework`
- [x] Testbericht und XCFramework als Artefakt
- [x] Kotlin/Native-Cache (`~/.konan`) und Gradle-Cache, damit Läufe schneller werden
- [ ] Screenshots per `xcrun simctl io booted screenshot` als Artefakt, sobald `ios/` existiert
- [ ] `xcodebuild build test` mit `CODE_SIGNING_ALLOWED=NO` für die App selbst

### 3. Testen ohne Gerät

- Shared-Tests im Simulator (`iosSimulatorArm64Test`) – läuft bereits.
- UI-/Screenshot-Tests im Simulator auf dem Runner.
- Optional Geräte-Cloud (BrowserStack, AWS Device Farm) – Konditionen für Open Source prüfen.
- Echte Geräte: Community-Tester über einen öffentlichen TestFlight-Link.

### 4. Signing und Verteilung komplett per CI

- Apple Developer Program (99 €/Jahr) ist für TestFlight und App Store unvermeidbar,
  ein Gerät aber nicht.
- Zertifikate und Profile per App-Store-Connect-API-Key (`.p8`) und fastlane
  (`cert`/`sigh` oder `match`) erzeugen; Secrets wie in `docs/ios-release.md`.
- Offen: Lässt sich die Developer-Registrierung ohne Apple-Gerät abschließen?
  (Zwei-Faktor über Telefonnummer ist möglich, ein iPhone ist nicht zwingend.)

### 5. Plattformunterschiede

- Updates über den App Store statt APK-Download.
- `LanBridge`/LAN-Info und Offline-Cache (Room) brauchen iOS-Entsprechungen.
- Vote-Seiten in `SFSafariViewController`.
- Texte: `app/ui/Strings.kt` (Deutsch/Englisch) ist die Vorlage für die SwiftUI-Seite.

## Akzeptanzkriterien

- [x] `shared` baut für `iosArm64` und `iosSimulatorArm64` in CI
- [x] `ios.yml` lädt das XCFramework als Artefakt hoch
- [ ] `ios/` existiert, `ios.yml` baut die App für den Simulator und lädt Screenshots hoch
- [ ] Rangliste, Voten und Status funktionieren im Simulator
- [ ] Signierter Build landet per Workflow in TestFlight
- [ ] README und `docs/ios-release.md` entsprechen dem tatsächlichen Stand

## Offene Fragen

- SwiftUI oder Compose Multiplatform?
- Developer-Account ohne Apple-Gerät: wie registrieren?
- Wer testet echte Geräte (TestFlight-Community)?
