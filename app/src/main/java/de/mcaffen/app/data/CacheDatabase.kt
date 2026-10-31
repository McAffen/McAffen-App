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
import de.mcaffen.app.model.VoteSite
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "players", primaryKeys = ["name"])
data class PlayerEntity(val name: String, val score: Int, val previousScore: Int? = null)

@Entity(tableName = "vote_sites", primaryKeys = ["site"])
data class VoteSiteEntity(val site: String, val url: String)

@Entity(tableName = "server_status", primaryKeys = ["id"])
data class ServerStatusEntity(
    @androidx.room.ColumnInfo(name = "id") val id: Int = 0,
    val onlinePlayers: Int,
    val networkServers: Int,
    val messages: String,
    /** JSON-Cache der per-Server-Liste (List<ServerInfo>), siehe McAffenRepository. */
    val servers: String? = null,
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

    @Query("SELECT * FROM players ORDER BY score DESC")
    suspend fun currentPlayers(): List<PlayerEntity>

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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Bisheriger Punktestand für Trend-Anzeige (↑/↓) in der Rangliste.
        db.execSQL("ALTER TABLE players ADD COLUMN previousScore INTEGER")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Der Status-Endpunkt liefert mittlerweile die Server-Liste; als JSON cachen.
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS server_status_new (" +
                "`id` INTEGER NOT NULL, " +
                "`onlinePlayers` INTEGER NOT NULL, " +
                "`networkServers` INTEGER NOT NULL, " +
                "`messages` TEXT NOT NULL, " +
                "`servers` TEXT, " +
                "PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "INSERT INTO server_status_new (id, onlinePlayers, networkServers, messages, servers) " +
                "SELECT id, onlinePlayers, networkServers, messages, NULL FROM server_status",
        )
        db.execSQL("DROP TABLE server_status")
        db.execSQL("ALTER TABLE server_status_new RENAME TO server_status")
    }
}

@Database(
    entities = [PlayerEntity::class, VoteSiteEntity::class, ServerStatusEntity::class, LegalCacheEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

fun PlayerEntity.toModel() = PlayerStat(name, score, previousScore)
fun VoteSiteEntity.toModel() = VoteSite(site, url)
