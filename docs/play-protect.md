# Play Protect meldet die McAffen-App als „schädlich“ – was tun?

## Warum passiert das?

Google Play Protect scanner jedes APK, das **außerhalb des Play Stores** installiert wird
(„Internet-Sideloading“: Browser, Messenger, direkte APK-Downloads). Die McAffen-App
erfüllt dabei genau das Muster, das den Heuristiken verdächtig erscheint:

- **APK-Download + Selbst-Installation** (`REQUEST_INSTALL_PACKAGES`, Download aus dem Internet,
  FileProvider, Installer-Intent) – das Laufzeitmuster eines „Droppers".
- **Neue, unbekannte App**: Reputation baut sich anhand des Signaturzertifikats und der
  Installationsbasis über Wochen auf. Bis dahin entscheidet die Heuristik.
- **Instabile Signatur**: Ohne konfigurierte GitHub-Signing-Secrets generiert CI bei jedem
  Release einen Zufalls-Key → für den Scanner ist jede Version ein „anderer Entwickler“
  und die Reputation beginnt bei null.

Das ist eine **Fehlwarnung** – die App enthält keinen schädlichen Code. Aber sie lässt sich
nicht komplett „wegprogrammieren": Die Berechtigung und das Install-Muster sind Voraussetzung
für In-App-Updates. Es gibt aber reduzierende und dauerhafte Maßnahmen.

## Für Nutzer: Warnung übergehen

1. Beim Installieren erscheint „Play Protect“-Dialog → **„Details“** (bzw. „Mehr anzeigen“) tippen.
2. **„Trotzdem installieren“** wählen. Der Nutzerdialog ist einmalig pro Version.
3. Falls die App anschließend gesperrt wird: **Play Store → Profil → Play Protect →** Scan des
   Geräts ausführen und die McAffen-App über „Berichten/Feedback“ als unbedenklich melden.
   Die Cloud-Einstufung aktualisiert sich meist binnen Stunden bis Tagen.

## Für Entwickler: Fehlwarnung dauerhaft beheben

1. **Stabile Signatur – der wichtigste Hebel.** Die 4 GitHub-Secrets hinterlegen
   (`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
   `RELEASE_KEY_PASSWORD` – Anleitung: [release-signing.md](release-signing.md)).
   Jedes Release wird dann mit demselben Zertifikat signiert, Google kann Reputation
   aufbauen, und die In-App-Updates prüfen die Signatur vorab (Signatur-Mismatch wird
   in der App blockiert, bevor es zum Fehlversuch kommt).

2. **Offizielle Fehlwarnungs-Beschwerde bei Google einreichen**
   (Play Protect Appeal): [support.google.com/googleplay/android-developer/contact/protectappeals](https://support.google.com/googleplay/android-developer/contact/protectappeals)
   - APK/SHA-256 angeben – die Prüfsumme jeder Version steht im Release-Body auf GitHub
     (das Release-Workflow hängt sie automatisch an) und als Asset `McAffen-release.apk.sha256`.
   - Referenz ergänzen: [Developer Guidance for Google Play Protect Warnings](https://developers.google.com/android/play-protect/warning-dev-guidance).
   - Bearbeitung dauert erfahrungsgemäß wenige Tage bis wenige Wochen.

3. **Transparenz**: In jedem Release steht jetzt SHA-256 + Play-Protect-Hinweis; der
   Update-Dialog in der App wartet den Nutzer direkt an die erwartbare Warnung heran.
   Das senkt die Abbruchquote und die „meine App löscht sich“-Supportanfragen.

4. **Langfristig (2026/2027)**: Android verknüpft Sideloading mit einer verifizierten
   Entwickler-Identität (Android Developer Console, Registrierung + Upload eines mit dem
   Release-Key signierten APK). Danach sinkt die Warnschwelle für registrierte Apps deutlich
   – ein Registration-Datum beobachten und frühzeitig registrieren. Quelle:
   [developer.android.com/developer-verification](https://developer.android.com/developer-verification).

## Was ausdrücklich NICHT hilft

- **Package-Renaming** pro Version: hilft der Reputation nicht, springt jeden Release auf null zurück.
- **Berechtigungen entfernen**: `REQUEST_INSTALL_PACKAGES` ist technisch Unverzichtbar für
  das In-App-Update; ohne die Funktion eliminiert man das Muster nur, indem man die
  In-App-Update-Funktion streicht (dann bräuchte jedes Update manuelles Deinstall +
  Installieren – genau, was vermieden werden sollte).
- **Signaturschlüssel ändern**: fuerzas Deinstallationen (Signature-Mismatch) und setzt
  Reputation null.

## Kurzfassung

Fehlwarnung = normal für „Crack-app-like“ Update-Muster außerhalb des Play Store. Sofort:
Nutzer über Dialogtext und Release-Hinweis aufle Rollen; dauerhaft: stabile Signatur via
GitHub-Secrets + Play-Protect-Appeal bei Google; künftig: Developer-Verification-Registrierung.
