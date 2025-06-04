package com.tmdb.movie.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.tmdb.movie.data.ContentId
import com.tmdb.movie.data.toRawString
import com.tmdb.movie.repository.IWatchHistoryRepository
import com.tmdb.movie.repository.WatchHistoryRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WebViewActivity : ComponentActivity(){
    @Inject
    lateinit var watchHistoryRepository: IWatchHistoryRepository

    private lateinit var webView: WebView
    private lateinit var contentId: ContentId
    private lateinit var rawContentId: String

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        window.decorView.post {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }

        // Recupera parametri passati dall’intent
        val url = intent.getStringExtra("url") ?: ""
        val tmdbId = intent.getIntExtra("tmdbId", -1)
        val season = intent.getIntExtra("season", -1)
        val episode = intent.getIntExtra("episode", -1)

        contentId = if (season >= 0 && episode >= 0) {
            ContentId.TvEpisode(tmdbId, season, episode)
        } else {
            ContentId.Movie(tmdbId)
        }
        rawContentId = contentId.toRawString()
        Log.e("WebViewActivity", "rawContentId: $rawContentId")

        val frameLayout = FrameLayout(this)
        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            addJavascriptInterface(PlayerBridge(), "AndroidBridge")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val reqUrl = request.url.toString()
                    return reqUrl.startsWith("intent://") || reqUrl.contains("ak.amskipoomr.com")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    // Recupera il minutaggio precedente e imposta la posizione
                    lifecycleScope.launch {
                        val previous = watchHistoryRepository.getWatchEntry(rawContentId).firstOrNull()
                        val resumeSec = (previous?.positionMs ?: 0L) / 1000.0
                        webView.evaluateJavascript(
                            """
                            (function() {
                                const v = document.querySelector('video');
                                if (!v) return;
                                v.currentTime = $resumeSec;
                                v.addEventListener('timeupdate', () => {
                                    AndroidBridge.onTimeUpdate(v.currentTime);
                                });
                                v.addEventListener('ended', () => {
                                    AndroidBridge.onEnded();
                                });
                            })();
                            """.trimIndent(),
                            null
                        )
                    }
                }
            }
            webChromeClient = WebChromeClient()
            loadUrl(url)
        }

        frameLayout.addView(webView)
        setContentView(frameLayout)
    }

    inner class PlayerBridge {

        @JavascriptInterface
        fun onTimeUpdate(currentTime: Float) {
            val timeMs = (currentTime * 1000).toLong()
            lifecycleScope.launch {
                watchHistoryRepository.saveWatchProgress(
                    contentId = rawContentId,
                    positionMs = timeMs,
                    finished = false
                )

                Log.e("WebViewActivity", "onTimeUpdate: $rawContentId - $timeMs")
            }
        }

        @JavascriptInterface
        fun onEnded() {
            lifecycleScope.launch {
                watchHistoryRepository.saveWatchProgress(
                    contentId = rawContentId,
                    positionMs = 0L,
                    finished = true
                )

                Log.e("WebViewActivity", "onEnded: $rawContentId")
            }
        }
    }
}