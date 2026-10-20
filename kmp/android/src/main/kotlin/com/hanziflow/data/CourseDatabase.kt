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

@Dao
interface CourseDao {
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

@Database(entities = [LessonEntity::class, SettingEntity::class, PlaybackEntity::class], version = 2, exportSchema = false)
abstract class CourseDatabase : RoomDatabase() {
    abstract fun dao(): CourseDao

    companion object {
        fun open(context: Context): CourseDatabase {
            val courseDir = context.filesDir.resolve("course").apply { mkdirs() }
            return Room.databaseBuilder(context, CourseDatabase::class.java, courseDir.resolve("state.db").absolutePath)
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lessons ADD COLUMN downloadedCatalogJson TEXT")
            }
        }
    }
}
