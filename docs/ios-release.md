# iOS-Version veröffentlichen

Die iOS-App teilt sich den Datenkern mit Android: das Kotlin-Modul `shared/`
spricht die mcaffen.de-API an, die SwiftUI-App in `ios/` ist die Oberfläche.

```
shared/            gemeinsamer Kotlin-Kern (Modelle, API, Vote-Seiten, Update-Logik)
  commonMain/      Code für beide Plattformen
  androidMain/     OkHttp-Engine
  iosMain/         NSURLSession-Engine
ios/
  McAffen/         SwiftUI-App (Rangliste, Voten, Status, Einstellungen)
  project.yml      XcodeGen-Beschreibung → erzeugt McAffen.xcodeproj
  ExportOptions.plist
.github/workflows/ios.yml   Manueller iOS-Build/TestFlight und paralleler iOS-Build bei jedem Cross-platform-Release
```

## 1. Lokal starten (braucht einen Mac)

```bash
brew install xcodegen
cd ios && xcodegen generate
open McAffen.xcodeproj
```

Xcode baut das Kotlin-Framework automatisch vor (Build-Phase
`embedAndSignAppleFrameworkForXcode`). Einmalig nötig: in den Signing-Einstellungen
dein Apple-Team wählen.

## 2. Was Apple verlangt

| Punkt | Wo | Hinweis |
|---|---|---|
| Apple Developer Program | developer.apple.com | 99 € pro Jahr, Voraussetzung für App Store und TestFlight |
| Bundle-ID | `de.mcaffen.app` | in `ios/project.yml` änderbar |
| App-Icon | `Assets.xcassets/AppIcon` | 1024×1024 PNG, ohne Transparenz |
| Datenschutzerklärung | App Store Connect | Pflicht-URL; die App sendet nur Anfragen an mcaffen.de und GitHub |
| Altersfreigabe | App Store Connect | Fragebogen, für eine Server-App unkritisch |


## 2b. Wenn der Build einen eigenen Mac benutzt

Die Standard-Workflows nutzen einen GitHub-gehosteten macOS-Runner
(`runs-on: macos-13`). Falls du stattdessen einen selbst gehosteten Mac einrichten
möchtest, ändere in `.github/workflows/ios.yml` beide `runs-on`-Einträge auf
`[self-hosted, macOS]` und registriere den Mac einmalig:

```bash
mkdir actions-runner && cd actions-runner
curl -O -L https://github.com/actions/runner/releases/latest/download/actions-runner-osx-arm64.tar.gz
tar xzf actions-runner-osx-arm64.tar.gz
./config.sh --url https://github.com/McAffen/McAffen-App --token <TOKEN>
./run.sh          # oder: ./svc.sh install && ./svc.sh start
```

Den `<TOKEN>` holst du unter
`Settings → Actions → Runners → New self-hosted runner`.

Der Mac braucht: Xcode (aktuelle Version), JDK 17, `brew install xcodegen`.
Danach greift der iOS-Build sofort.

**Alternative ohne Runner:** lokal bauen –
`cd ios && xcodegen generate && open McAffen.xcodeproj`

## 3. TestFlight über GitHub Actions

Einmalig diese Secrets im Repo setzen (`Settings → Secrets and variables → Actions`):

| Secret | Inhalt |
|---|---|
| `IOS_CERT_BASE64` | Verteilungs-Zertifikat (.p12) als Base64 |
| `IOS_CERT_PASSWORD` | Passwort des .p12 |
| `IOS_PROFILE_BASE64` | Provisioning-Profil (.mobileprovision) als Base64 |
| `APP_STORE_CONNECT_KEY_ID` | Key-ID des App-Store-Connect-API-Keys |
| `APP_STORE_CONNECT_ISSUER_ID` | Issuer-ID |
| `APP_STORE_CONNECT_KEY` | Inhalt der `.p8`-Datei |

Base64 erzeugen:

```bash
base64 -i zertifikat.p12 | pbcopy
base64 -i profil.mobileprovision | pbcopy
```

Zusätzlich die Repository-Variable `ENABLE_TESTFLIGHT` auf `true` setzen.
Danach `Actions → iOS Build → Run workflow` starten – der Job `testflight`
archiviert die App und lädt sie zu App Store Connect hoch.

## 4. Was in der iOS-App anders ist als auf Android

- **Updates** laufen über den App Store. Die App prüft nur, ob eine neuere
  Version existiert, und verweist dorthin – kein In-App-Download wie auf Android.
- **Vote-Seiten** öffnen sich in `SFSafariViewController` (eingebauter Browser)
  oder in Safari; umschaltbar in den Einstellungen.
- **LAN-Welt**: dieselben Werte und Schritte wie auf Android (`LanInfo`).

## 5. Offen vor der ersten Einreichung

- App-Icon und Launch-Screen-Assets in Xcode ergänzen (Ordner
  `ios/McAffen/Assets.xcassets`).
- Screenshots für App Store Connect (6,7" und 6,1" iPhone).
- Bundle-ID bei Apple registrieren und mit dem Profil verknüpfen.
