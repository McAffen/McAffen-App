# McAffen App (Android + iOS)

App für `mcaffen.de` mit Rangliste, Voten und Serverstatus – für Android
(Jetpack Compose) und iOS (SwiftUI). Der Datenkern liegt geteilt in `shared/`
(Kotlin Multiplatform).

- **Android:** `app/` – Jetpack Compose, Room-Cache, In-App-Update
- **iOS:** native SwiftUI-App in `ios/`; der macOS-Workflow baut sie für den iOS-Simulator und lädt sie als Artefakt hoch
- **Sprachen:** Deutsch und Englisch, umschaltbar unter Einstellungen → Sprache („System“ folgt dem Gerät). Alle Texte liegen in `app/src/main/java/de/mcaffen/app/ui/Strings.kt`
- **Gemeinsam:** `shared/` – Modelle, API-Zugriff (Ktor), Vote-Seiten, Update-Logik
- **iOS veröffentlichen:** siehe [docs/ios-release.md](docs/ios-release.md)

## Architektur   
    
- **UI:** Jetpack Compose Material 3 mit Bottom Navigation
- **Datenzugriff:** Retrofit/Moshi gegen `https://mcaffen.de/{rangliste,voten,status}`; Ranglistenplätze werden angezeigt und Vote-Links erst nach erfolgreicher Spielersuche sowie HTTPS-Prüfung freigeschaltet
- **Datenschutz:** Die App enthält keine Zugangsdaten und protokolliert keine Netzwerk-URLs oder Spielernamen
- **Offline-Fallback:** Room speichert den letzten erfolgreichen Abruf
- **Updates:** Beim Start wird das neueste GitHub-Release geprüft; bei einer höheren Version erscheint ein Dialog mit dem Release-Link
- **CI/CD:** Pull Requests werden bei jedem Update gebaut und gelintet; Pushes auf `main` starten den Check ebenfalls. Tags wie `v1.0.0` erzeugen ein GitHub Release
- **Offene Vorhaben:** als Issue-Vorlagen in [`docs/issues/`](docs/issues) – iOS ohne eigenes Gerät und Mehrsprachigkeit

Die API-Endpunkte sind derzeit gemäß der vorgegebenen Annahme modelliert. Falls die Website abweichende JSON-Formate liefert, müssen nur die Modelle bzw. Retrofit-Adapter angepasst werden.

## Lokal bauen

```bash
./gradlew test lint assembleDebug
```

Der Release-Workflow veröffentlicht ein signiertes, installierbares APK. Für stabile Updates sollte ein Keystore über die GitHub Secrets
`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` und `RELEASE_KEY_PASSWORD` hinterlegt werden. Ohne diese Secrets
erzeugt der Workflow als Fallback einen temporären Schlüssel; die APK ist installierbar, aber spätere Releases können dann nicht als Update
über die vorherige Installation installiert werden.

<!-- Signing-Test: Release-Build mit festem Keystore -->
