package com.hanziflow.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ProgressRepositoryTest {
    @Test
    fun `position survives a new repository instance and end resets it`() = runTest {
        val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), CourseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            ProgressRepositoryImpl(database).savePosition("lesson_002", "noi", 42_000)
            assertEquals(42_000, ProgressRepositoryImpl(database).positionMs("lesson_002", "noi"))

            ProgressRepositoryImpl(database).savePosition("lesson_002", "noi", 0)
            assertEquals(0, ProgressRepositoryImpl(database).positionMs("lesson_002", "noi"))
        } finally {
            database.close()
        }
    }
}
