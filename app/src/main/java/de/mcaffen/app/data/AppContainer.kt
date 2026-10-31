package de.mcaffen.app.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.mcaffen.app.network.McAffenApi
import de.mcaffen.app.network.GitHubReleaseApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        CacheDatabase::class.java,
        "mcaffen-cache.db",
    ).build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mcaffen.de/api/")
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            ),
        )
        .build()

    private val githubRetrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            ),
        )
        .build()

    val repository = McAffenRepository(
        api = retrofit.create(McAffenApi::class.java),
        releaseApi = githubRetrofit.create(GitHubReleaseApi::class.java),
        dao = database.cacheDao(),
    )
}
