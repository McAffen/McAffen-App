import java.util.Properties

/** Höchster bereits veröffentlichter versionCode (v1.0.35 = 1.000.035) – nie unterschreiten. */
val MIN_VERSION_CODE = 1_000_035

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "de.mcaffen.app"
    compileSdk = 35

    // Versionierung: Im Release-Workflow setzt `./gradlew -PmcaffenTag=v1.0.7 ...`
    // automatisch versionName ("1.0.7") und versionCode (major*1.000.000 +
    // minor*1.000 + patch). Ohne Property (lokale Builds, CI) gilt 1.0.0 oder
    // äquivalent versionCode 1.000.000 – Tags erhöhen den Code automatisch.
    val releaseTag: String? = providers.gradleProperty("mcaffenTag").orNull
    val releaseVersionName: String =
        releaseTag?.trim()?.removePrefix("v")?.takeIf(String::isNotBlank) ?: "1.0.0"
    val versionParts = releaseVersionName.split('.')
        .map { part -> part.filter(Char::isDigit).takeIf(String::isNotEmpty)?.toIntOrNull() ?: 0 }
    val paddedParts = versionParts.take(3) + List(maxOf(0, 3 - versionParts.size)) { 0 }
    // Patch-Anteil darf bis 999.999 gehen (Datumsversionen wie 1.251118.1234),
    // damit jede Veröffentlichung einen höheren versionCode hat als die vorherige.
    val computedVersionCode: Int =
        paddedParts[0].coerceIn(0, 99) * 10_000_000 +
            paddedParts[1].coerceIn(0, 99) * 100_000 +
            paddedParts[2].coerceIn(0, 99_999)
    // Untergrenze: Die bereits verteilten Releases bis v1.0.35 hatten versionCode
    // 1.000.035. Ein kleinerer Code würde von Android als Update abgelehnt.
    val releaseVersionCode: Int = maxOf(computedVersionCode, MIN_VERSION_CODE)

    defaultConfig {
        applicationId = "de.mcaffen.app"
        minSdk = 24
        targetSdk = 35
        versionCode = releaseVersionCode
        versionName = releaseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // Signatur: derselbe Schlüssel für jede Release-APK, sonst verweigert Android
    // das Update über die installierte App. Quelle (in dieser Reihenfolge):
    //   1. CI: Keystore-Datei + Passwörter aus Umgebungsvariablen (GitHub Secrets)
    //   2. lokal: signing/keystore.properties + signing/mcaffen-release.keystore (gitignored)
    // Fehlt beides, entsteht eine unsignierte APK – der Release-Workflow bricht dann ab.
    val keystorePropertiesFile = rootProject.file("signing/keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { stream -> keystoreProperties.load(stream) }
    }
    fun signingValue(env: String, key: String): String? =
        System.getenv(env)?.takeIf(String::isNotBlank) ?: keystoreProperties.getProperty(key)?.takeIf(String::isNotBlank)

    val releaseStorePath = signingValue("RELEASE_KEYSTORE_PATH", "storeFile")
    val releaseStorePassword = signingValue("RELEASE_KEYSTORE_PASSWORD", "storePassword")
    val releaseKeyAlias = signingValue("RELEASE_KEY_ALIAS", "keyAlias")
    val releaseKeyPassword = signingValue("RELEASE_KEY_PASSWORD", "keyPassword")
    val hasReleaseSigning = releaseStorePath != null &&
        releaseStorePassword != null &&
        releaseKeyAlias != null &&
        releaseKeyPassword != null &&
        rootProject.file(releaseStorePath).exists()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    // Startbildschirm mit Markenlogo (androidx.core.splashscreen, API 21+).
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    // Eingebauter Browser für Vote-Seiten (Custom Tabs)
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hintergrund-Update-Prüfung (WorkManager): alle 12 h, nur mit Internet.
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // AsyncImage für Minecraft-Avatare (mc-heads.net, wie auch auf der Website).
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Tooling for Compose previews in the IDE.
    debugImplementation("androidx.compose.ui:ui-tooling")

    // NOTE: Test dependencies (junit, espresso, kotlinx-coroutines-test) are
    // intentionally omitted: the project currently has no unit or
    // instrumentation tests. Lint resolves the test compile classpaths and
    // failed on the CI runner when these unused artifacts were declared.
    // Re-add them together with real test sources.
}
