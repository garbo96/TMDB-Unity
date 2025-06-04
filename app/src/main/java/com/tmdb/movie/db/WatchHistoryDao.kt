package com.tmdb.movie.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tmdb.movie.data.WatchedEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watched WHERE contentId = :id")
    fun getById(id: String): Flow<WatchedEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entry: WatchedEntry)

    @Query("DELETE FROM watched WHERE contentId = :id")
    suspend fun deleteById(id: String)

}