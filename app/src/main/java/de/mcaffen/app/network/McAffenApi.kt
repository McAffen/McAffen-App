package de.mcaffen.app.network

import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.model.VoteSite
import retrofit2.http.GET

interface McAffenApi {
    @GET("rangliste")
    suspend fun getLeaderboard(): List<PlayerStat>

    @GET("voten")
    suspend fun getVoteSites(): List<VoteSite>

    @GET("status")
    suspend fun getServerStatus(): ServerStatus
}
