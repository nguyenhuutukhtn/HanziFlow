package com.hanziflow

import android.app.Application
import com.hanziflow.data.CourseDatabase
import com.hanziflow.data.CourseRepository

class HanziFlowApplication : Application() {
    val database by lazy { CourseDatabase.open(this) }
    val repository by lazy { CourseRepository(this, database) }
}

