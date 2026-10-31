package de.mcaffen.app.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.mcaffen.app.network.McAffenApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        CacheDatabase::class.java,
        "mcaffen-cache.db",
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val httpClient = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mcaffen.de/api/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    /** Einstellungen (Dark-Mode, Farbschema, Update-Bereitschaft), dauerhaft gespeichert. */
    val settings = SettingsStore(context)

    val repository = McAffenRepository(
        api = retrofit.create(McAffenApi::class.java),
        dao = database.cacheDao(),
        client = httpClient,
        moshi = moshi,
    )
}
