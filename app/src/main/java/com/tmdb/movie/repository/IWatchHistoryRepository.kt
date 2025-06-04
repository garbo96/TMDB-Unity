package com.tmdb.movie.repository

import com.tmdb.movie.data.WatchedEntry
import kotlinx.coroutines.flow.Flow

interface IWatchHistoryRepository {
    fun getWatchEntry(id: String): Flow<WatchedEntry?>
    suspend fun saveWatchProgress(contentId: String, positionMs: Long, finished: Boolean = false)
}