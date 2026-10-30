package de.mcaffen.app.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.mcaffen.app.network.McAffenApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        CacheDatabase::class.java,
        "mcaffen-cache.db",
    ).build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mcaffen.de/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
                .build(),
        )
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            ),
        )
        .build()

    val repository = McAffenRepository(
        api = retrofit.create(McAffenApi::class.java),
        dao = database.cacheDao(),
    )
}
