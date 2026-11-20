package de.mcaffen.app.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.mcaffen.app.network.McAffenApi
import de.mcaffen.app.ui.AppLanguage
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        CacheDatabase::class.java,
        "mcaffen-cache.db",
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    /** Einstellungen (Dark-Mode, Farbschema, Sprache, Update-Bereitschaft), dauerhaft gespeichert. */
    val settings = SettingsStore(context)

    /**
     * Hängt `?lang=en` an Anfragen an mcaffen.de, solange die App auf Englisch steht.
     * Die API liefert Meldungen, News, Spielmodi, FAQ und Regeln dann auf Englisch;
     * ohne Parameter bleibt es beim deutschen Standard.
     */
    private val languageInterceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url
        val english = AppLanguage.resolve(settings.language.value) == AppLanguage.ENGLISH
        if (english && url.host.endsWith("mcaffen.de") && url.queryParameter("lang") == null) {
            chain.proceed(request.newBuilder().url(url.newBuilder().addQueryParameter("lang", "en").build()).build())
        } else {
            chain.proceed(request)
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(languageInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mcaffen.de/api/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val repository = McAffenRepository(
        api = retrofit.create(McAffenApi::class.java),
        dao = database.cacheDao(),
        client = httpClient,
        moshi = moshi,
        language = { AppLanguage.resolve(settings.language.value) },
    )
}
