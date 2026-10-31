# Release-Signing einrichten

Damit Updates **über die installierte App hinweg installierbar** bleiben, muss jede
Release-APK mit **demselben Keystore** signiert werden. Ohne Secrets generiert der
Release-Workflow bei jedem Lauf einen zufälligen temporären Schlüssel – dann verweigert
Android das direkte Installing des Updates (Nutzer müssten zuerst deinstallieren).

## Einmalige Einrichtung

Das Keystore und alle Kennwörter liegen lokal im Ordner `signing/` (gitignored, wird
nicht committet):

| Datei | Inhalt |
|---|---|
| `signing/mcaffen-release.keystore` | Keystore (PKCS12), Alias `mcaffen` |
| `signing/mcaffen-release.keystore.base64` | Keystore als Base64 (Base64-Kodierung → `RELEASE_KEYSTORE_BASE64`) |
| `signing/key-password.txt` | Keystore- & Key-Passwort (identisch) |

### 1. Secrets in GitHub anlegen

Repository → **Settings → Secrets and variables → Actions → New repository secret**:

| Secret-Name | Wert |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Inhalt von `signing/mcaffen-release.keystore.base64` |
| `RELEASE_KEYSTORE_PASSWORD` | Passwort aus `signing/key-password.txt` |
| `RELEASE_KEY_ALIAS` | `mcaffen` |
| `RELEASE_KEY_PASSWORD` | Passwort aus `signing/key-password.txt` |

Die `.base64`-Datei enthält Zeilenumbrüche – beim Einfügen alle Zeilen zu einem
zusammenhängenden Text verbinden (ohne Umbrüche), damit der `base64 --decode`-Aufruf
im Workflow sicher funktioniert.

### 2. Keystore sicher aufbewahren

`signing/` in einen Passwort-Manager oder verschlüsselten Speicher spiegeln
(z. B. ZIP mit AES-Verschlüsselung als Backup im Passwort-Manager). **Bei Verlust des
Keystores kann keine signatur-kompatible Update-APK mehr gebaut werden.**

## Neues Keystore erzeugen (falls jemals nötig)

```powershell
keytool -genkeypair -v -keystore signing\mcaffen-release.keystore -storetype PKCS12 `
  -storepass <PASSWORT> -alias mcaffen -keypass <PASSWORT> `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -dname "CN=McAffen, OU=Android, O=McAffen, L=Berlin, S=Berlin, C=DE"
[Convert]::ToBase64String([IO.File]::ReadAllBytes("signing\mcaffen-release.keystore"), `
  [Base64FormattingOptions]::InsertLineBreaks) | Set-Content "signing\mcaffen-release.keystore.base64"
```

## Signatur-Wechsel

Da die bisherigen Releases mit zufälligen temporären Schlüsseln signiert waren, ist
die neue Keystore-Signatur **nicht** mit der vorherigen App-Installation kompatibel:
Nutzer mit bereits installierter App müssen einmal deinstallieren, bevor sie Updates
über die neue APK installieren können. Ein Hinweis im Release-Changelog der nächsten
APK reicht aus – danach gilt die Signatur dauerhaft konsistent.
