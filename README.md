# McAffen Android-App

Compose-MVP für `mcaffen.de` mit Rangliste, Voten und Serverstatus.

## Architektur

- **UI:** Jetpack Compose Material 3 mit Bottom Navigation
- **Datenzugriff:** Retrofit/Moshi gegen `https://mcaffen.de/{rangliste,voten,status}`
- **Offline-Fallback:** Room speichert den letzten erfolgreichen Abruf
- **Connector:** `app://connector_openai_deep_research` ist als Deep-Link registriert
- **CI/CD:** Pull Requests werden bei jedem Update gebaut und gelintet; Pushes auf `main` starten den Check ebenfalls. Tags wie `v1.0.0` erzeugen ein GitHub Release

Die API-Endpunkte sind derzeit gemäß der vorgegebenen Annahme modelliert. Falls die Website abweichende JSON-Formate liefert, müssen nur die Modelle bzw. Retrofit-Adapter angepasst werden.

## Lokal bauen

```bash
./gradlew test lint assembleDebug
```

Der Release-Workflow veröffentlicht ein signiertes, installierbares APK. Für stabile Updates sollte ein Keystore über die GitHub Secrets
`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` und `RELEASE_KEY_PASSWORD` hinterlegt werden. Ohne diese Secrets
erzeugt der Workflow als Fallback einen temporären Schlüssel; die APK ist installierbar, aber spätere Releases können dann nicht als Update
über die vorherige Installation installiert werden.
