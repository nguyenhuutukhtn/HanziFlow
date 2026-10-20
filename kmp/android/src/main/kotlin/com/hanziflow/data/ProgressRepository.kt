package com.hanziflow.data

import javax.inject.Inject

/**
 * Placeholder for progress sync (lesson_progress / test_attempts) work (D2).
 * No shape yet beyond the marker: don't speculatively design it before the
 * sign-in/sync task exists.
 */
interface ProgressRepository

class ProgressRepositoryImpl @Inject constructor() : ProgressRepository
