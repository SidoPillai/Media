package com.androidnomad.media

data class Video(
    val id: String,
    val title: String,
    val url: String,
    val isDownloaded: Boolean = false,
    val isDrmProtected: Boolean = false
)

val videoList = listOf(
    Video(
        id = "1",
        title = "Sintel Movie (DASH)",
        url = "https://bitmovin-a.akamaihd.net/content/sintel/sintel.mpd",
        isDownloaded = false,
        isDrmProtected = false
    ),
    Video(
        id = "2",
        title = "Big Buck Bunny (HLS)",
        url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isDownloaded = false,
        isDrmProtected = false
    ),
    Video(
        id = "3",
        title = "Widevine DRM Video",
        url = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
        isDownloaded = false,
        isDrmProtected = true
    )
)