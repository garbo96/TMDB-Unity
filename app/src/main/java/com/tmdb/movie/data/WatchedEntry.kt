package com.tmdb.movie.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watched")
data class WatchedEntry(
    @PrimaryKey val contentId: String,
    val positionMs: Long,
    val isFinished: Boolean,
    val updatedAt: Long
)