# McAffen Android-App

Compose-MVP für `mcaffen.de` mit Rangliste, Voten und Serverstatus.

## Architektur

- **UI:** Jetpack Compose Material 3 mit Bottom Navigation
- **Datenzugriff:** Retrofit/Moshi gegen `https://mcaffen.de/{rangliste,voten,status}`
- **Offline-Fallback:** Room speichert den letzten erfolgreichen Abruf
- **Connector:** `app://connector_openai_deep_research` ist als Deep-Link registriert
- **CI/CD:** Push/PR baut und lintet; Tags wie `v1.0.0` erzeugen ein GitHub Release

Die API-Endpunkte sind derzeit gemäß der vorgegebenen Annahme modelliert. Falls die Website abweichende JSON-Formate liefert, müssen nur die Modelle bzw. Retrofit-Adapter angepasst werden.

## Lokal bauen

```bash
./gradlew test lint assembleDebug
```

Das Release-Workflow-Artefakt ist aktuell ein nicht signiertes APK. Für Play-Store-Verteilung muss ein Keystore über GitHub Secrets eingebunden werden.
