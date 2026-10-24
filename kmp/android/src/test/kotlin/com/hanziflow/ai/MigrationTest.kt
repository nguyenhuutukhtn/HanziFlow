package com.hanziflow.ai

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hanziflow.data.CourseDatabase
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MigrationTest {
    @Test
    fun `MIGRATION_2_3 keeps lessons and adds an empty explain_threads table`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val file = File(context.cacheDir, "v2.db").apply { delete() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { raw ->
            raw.execSQL("CREATE TABLE `lessons` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `level` TEXT NOT NULL, `lessonOrder` INTEGER NOT NULL, `remoteVersion` INTEGER NOT NULL, `downloadedVersion` INTEGER, `downloadedCatalogJson` TEXT, `catalogJson` TEXT NOT NULL, `downloadState` TEXT NOT NULL, `error` TEXT, PRIMARY KEY(`id`))")
            raw.execSQL("CREATE TABLE `settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
            raw.execSQL("CREATE TABLE `playback` (`slot` INTEGER NOT NULL, `lessonId` TEXT NOT NULL, `track` TEXT NOT NULL, `positionMs` INTEGER NOT NULL, `speed` REAL NOT NULL, PRIMARY KEY(`slot`))")
            raw.execSQL("INSERT INTO lessons VALUES ('lesson_002','L2','A1',2,4,NULL,NULL,'{}','AVAILABLE',NULL)")
            raw.version = 2
        }
        val db = Room.databaseBuilder(context, CourseDatabase::class.java, file.absolutePath)
            .addMigrations(CourseDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
        try {
            assertEquals("L2", db.dao().lesson("lesson_002")?.title)
            assertNull(db.dao().explainThread("lesson_002", "l1"))
            db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM explain_threads").use {
                it.moveToFirst()
                assertEquals(0, it.getInt(0))
            }
            assertNotNull(db.dao())
        } finally {
            db.close()
        }
    }
}
