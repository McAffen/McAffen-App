package de.mcaffen.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.model.VoteSite
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "players", primaryKeys = ["name"])
data class PlayerEntity(val name: String, val score: Int)

@Entity(tableName = "vote_sites", primaryKeys = ["site"])
data class VoteSiteEntity(val site: String, val url: String)

@Entity(tableName = "server_status", primaryKeys = ["id"])
data class ServerStatusEntity(
    @androidx.room.ColumnInfo(name = "id") val id: Int = 0,
    val onlinePlayers: Int,
    val networkServers: Int,
    val messages: String,
)

/** Zuletzt von mcaffen.de synchronisierter Rechtstext (Impressum, Datenschutz, AGB). */
@Entity(tableName = "legal_cache", primaryKeys = ["id"])
data class LegalCacheEntity(
    val id: String,
    val title: String,
    val html: String,
    val updatedAt: Long,
)

@Dao
interface CacheDao {
    @Query("SELECT * FROM players ORDER BY score DESC")
    fun players(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM vote_sites ORDER BY site")
    fun voteSites(): Flow<List<VoteSiteEntity>>

    @Query("SELECT * FROM server_status WHERE id = 0")
    fun status(): Flow<ServerStatusEntity?>

    @Query("SELECT * FROM legal_cache")
    fun legalCache(): Flow<List<LegalCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayers(players: List<PlayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVoteSites(sites: List<VoteSiteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStatus(status: ServerStatusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLegal(items: List<LegalCacheEntity>)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS legal_cache (" +
                "`id` TEXT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`html` TEXT NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))",
        )
    }
}

@Database(
    entities = [PlayerEntity::class, VoteSiteEntity::class, ServerStatusEntity::class, LegalCacheEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

fun PlayerEntity.toModel() = PlayerStat(name, score)
fun VoteSiteEntity.toModel() = VoteSite(site, url)
fun ServerStatusEntity.toModel() = ServerStatus(onlinePlayers, networkServers, messages.lines())
