package com.tmdb.movie.data

sealed class ContentId {
    data class Movie(val tmdbId: Int) : ContentId()
    data class TvEpisode(val tmdbId: Int, val season: Int, val episode: Int) : ContentId()
}

fun ContentId.toRawString(): String = when (this) {
    is ContentId.Movie -> "movie_$tmdbId"
    is ContentId.TvEpisode -> "tv_${tmdbId}_s${season}_e${episode}"
}

fun parseContentId(contentId: String): ContentId? {
    return when {
        contentId.startsWith("movie_") -> {
            val id = contentId.removePrefix("movie_").toIntOrNull()
            id?.let { ContentId.Movie(it) }
        }

        contentId.startsWith("tv_") -> {
            val regex = Regex("""tv_(\d+)_s(\d+)_e(\d+)""")
            val match = regex.matchEntire(contentId)
            match?.let {
                val (id, season, episode) = it.destructured
                ContentId.TvEpisode(id.toInt(), season.toInt(), episode.toInt())
            }
        }

        else -> null
    }
}