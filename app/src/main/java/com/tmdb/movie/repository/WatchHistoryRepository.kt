package com.tmdb.movie.repository

import com.tmdb.movie.common.Dispatcher
import com.tmdb.movie.common.TMDBDispatchers
import com.tmdb.movie.data.WatchedEntry
import com.tmdb.movie.db.WatchHistoryDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WatchHistoryRepository @Inject constructor(
    private val dao: WatchHistoryDao,
    @Dispatcher(TMDBDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : IWatchHistoryRepository {

    override fun getWatchEntry(id: String): Flow<WatchedEntry?> =
        dao.getById(id)

    override suspend fun saveWatchProgress(contentId: String, positionMs: Long, finished: Boolean) {
        withContext(ioDispatcher) {
            dao.save(
                WatchedEntry(
                    contentId = contentId,
                    positionMs = positionMs,
                    isFinished = finished,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}