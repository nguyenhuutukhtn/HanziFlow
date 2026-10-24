package com.hanziflow.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lessons", primaryKeys = ["id"])
data class LessonEntity(
    val id: String,
    val title: String,
    val level: String,
    val lessonOrder: Int,
    val remoteVersion: Int,
    val downloadedVersion: Int? = null,
    val downloadedCatalogJson: String? = null,
    val catalogJson: String,
    val downloadState: String = "AVAILABLE",
    val error: String? = null,
)

@Entity(tableName = "settings", primaryKeys = ["key"])
data class SettingEntity(val key: String, val value: String)

@Entity(tableName = "playback", primaryKeys = ["slot"])
data class PlaybackEntity(
    val slot: Int = 1,
    val lessonId: String,
    val track: String,
    val positionMs: Long,
    val speed: Float,
)

/** One AI explanation thread per lesson line; `messagesJson` excludes the system message. */
@Entity(tableName = "explain_threads", primaryKeys = ["lessonId", "lineId"])
data class ExplainThreadEntity(
    val lessonId: String,
    val lineId: String,
    val hanzi: String,
    val lessonVersion: Int,
    val messagesJson: String,
    val updatedAt: Long,
)

@Dao
interface CourseDao {
    @Query("SELECT * FROM explain_threads WHERE lessonId = :lessonId AND lineId = :lineId")
    fun observeExplainThread(lessonId: String, lineId: String): Flow<ExplainThreadEntity?>

    @Query("SELECT * FROM explain_threads WHERE lessonId = :lessonId AND lineId = :lineId")
    suspend fun explainThread(lessonId: String, lineId: String): ExplainThreadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putExplainThread(thread: ExplainThreadEntity)

    @Query("SELECT * FROM lessons ORDER BY lessonOrder")
    fun observeLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun lesson(id: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putLesson(lesson: LessonEntity)

    @Query("UPDATE lessons SET downloadState = :state, error = :error WHERE id = :id")
    suspend fun setDownloadState(id: String, state: String, error: String? = null)

    @Query("UPDATE lessons SET downloadedVersion = :version, downloadedCatalogJson = :catalogJson, downloadState = 'DOWNLOADED', error = NULL WHERE id = :id")
    suspend fun markDownloaded(id: String, version: Int, catalogJson: String)

    @Query("UPDATE lessons SET downloadedVersion = NULL, downloadedCatalogJson = NULL, downloadState = 'AVAILABLE', error = NULL WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun setting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSetting(setting: SettingEntity)

    @Query("SELECT * FROM playback WHERE slot = 1")
    suspend fun playback(): PlaybackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putPlayback(playback: PlaybackEntity)
}

@Database(entities = [LessonEntity::class, SettingEntity::class, PlaybackEntity::class, ExplainThreadEntity::class], version = 3, exportSchema = false)
abstract class CourseDatabase : RoomDatabase() {
    abstract fun dao(): CourseDao

    companion object {
        fun open(context: Context): CourseDatabase {
            val courseDir = context.filesDir.resolve("course").apply { mkdirs() }
            return Room.databaseBuilder(context, CourseDatabase::class.java, courseDir.resolve("state.db").absolutePath)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `explain_threads` (`lessonId` TEXT NOT NULL, `lineId` TEXT NOT NULL, " +
                        "`hanzi` TEXT NOT NULL, `lessonVersion` INTEGER NOT NULL, `messagesJson` TEXT NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, PRIMARY KEY(`lessonId`, `lineId`))",
                )
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lessons ADD COLUMN downloadedCatalogJson TEXT")
            }
        }
    }
}
