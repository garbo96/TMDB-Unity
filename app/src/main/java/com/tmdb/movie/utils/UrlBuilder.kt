package com.tmdb.movie.utils

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb

object UrlBuilder {
    private const val AUTHORITY = "vixsrc.to"
    private var primaryColor: String? = null

    @Composable
    fun init(){
        primaryColor = getPrimaryColorHex()
    }

    fun movie(tmdbId: String, lang: String? = null, startAt:String? = null): String {

        val url = Uri.Builder().apply{
            scheme("https")
            authority(AUTHORITY)
            appendPath("movie")
            appendPath(tmdbId)
            appendQueryParameter("autoplay", "true")
            if (!startAt.isNullOrEmpty()) {
                appendQueryParameter("startAt", startAt)
            }
            if (!lang.isNullOrEmpty()) {
                appendQueryParameter("lang", startAt)
            }

            if(!primaryColor.isNullOrEmpty())
                appendQueryParameter("primaryColor", primaryColor)

            build()
        }.toString()

        return url
    }

    fun tv(tmdbId: String, season: String, episode: String, lang: String? = null, startAt:String? = null): String {

        val url = Uri.Builder().apply{
            scheme("https")
            authority(AUTHORITY)
            appendPath("tv")
            appendPath(tmdbId)
            appendPath(season)
            appendPath(episode)
            appendQueryParameter("autoplay", "true")
            if (!startAt.isNullOrEmpty()) {
                appendQueryParameter("startAt", startAt)
            }
            if (!lang.isNullOrEmpty()) {
                appendQueryParameter("lang", startAt)
            }

            if(!primaryColor.isNullOrEmpty())
                appendQueryParameter("primaryColor", primaryColor)

            build()
        }.toString()

        return url
    }

    @Composable
    private fun getPrimaryColorHex(): String {
        val color = MaterialTheme.colorScheme.primary
        return String.format("%06X", 0xFFFFFF and color.toArgb())
    }
}

