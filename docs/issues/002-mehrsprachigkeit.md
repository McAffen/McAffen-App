# [Feature] Mehrsprachigkeit (Deutsch/Englisch) mit Sprachwahl in den Einstellungen

**Labels:** `enhancement`, `i18n`, `ui`

## Ausgangslage

Alle Texte der Android-App standen fest im Kotlin-Code (Compose), verteilt über
`ui/McAffenApp.kt`, `ui/Dialogs.kt`, `ui/screens/*` und `ui/components/*`.
Eine Sprachwahl gab es nicht; die App war fest deutsch.

## Ziel

Deutsch und Englisch, umschaltbar in den Einstellungen, mit „System“ als Standard
(folgt der Gerätesprache). Die Texte liegen an einer Stelle, damit die spätere
iOS-Oberfläche dieselben Schlüssel benutzen kann.

## Umsetzung (Stand dieser Änderung)

- **`app/src/main/java/de/mcaffen/app/ui/Strings.kt`** enthält die Tabelle
  Schlüssel → (Deutsch, Englisch), die Sprache (`AppLanguage`: `de`, `en`, `SYSTEM`)
  und `Strings`, das die Texte in der aktiven Sprache liefert.
- **`LocalStrings`** ist ein `CompositionLocal`: `McAffenApp` setzt es anhand der
  Einstellung, alle Composables lesen darüber (`LocalStrings.current["tab.news"]`
  oder `formatString("subtitle.players", 5)`).
- **Einstellungen → Sprache** bietet „System“, „Deutsch“ und „English“.
  Die Wahl wird in `SettingsStore` gespeichert (`KEY_LANGUAGE`).
- Übersetzt sind: Navigation, Rangliste, Voten, News, Spielerprofil, Status
  (inklusive LAN-Welt-Schritte), Einstellungen, Über-Dialog, Update-Dialog und
  der Willkommens-Flow. Fehlt ein Eintrag, erscheint der Schlüssel – der Test
  `StringsTest` prüft, dass jeder benutzte Schlüssel in beiden Sprachen existiert
  und die Platzhalter (`%s`, `%d`) übereinstimmen.
- **Nicht übersetzt:** die Rechtstexte (Impressum, Datenschutz, AGB). Sie stammen
  von mcaffen.de bzw. sind verbindlich deutsch; die Übersetzung muss juristisch
  geprüft werden.

## Akzeptanzkriterien

- [x] Sprachwahl in den Einstellungen, gespeichert über App-Neustarts
- [x] „System“ folgt der Gerätesprache (deutsch → Deutsch, sonst Englisch)
- [x] Alle Oberflächentexte außer den Rechtstexten übersetzt
- [x] Unit-Test prüft Vollständigkeit und Platzhalter beider Sprachen
- [ ] Screenshots der englischen Oberfläche für den App Store (offen, iOS)

## Offene Fragen

- Weitere Sprachen (z. B. Türkisch, Spanisch)? Die Tabelle ist dafür vorbereitet.
- Sollen die Rechtstexte eine englische Fassung bekommen?
