package com.androidnomad.media

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class DownloadViewModel @Inject constructor(
    downloadService: MediaDownloadService
) : ViewModel() {

    private var downloadManager: DownloadManager = downloadService.downloadMgr

    // Track video download states
    private val _downloadStates = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadStates: StateFlow<Map<String, Int>> = _downloadStates

    // Download Progress
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress

    init {
        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                Log.d("DownloadManager", "Download status changed: ${download.request.id} -> ${download.state}")

                // Update download state in the ViewModel
                viewModelScope.launch {
                    _downloadStates.value = _downloadStates.value.toMutableMap().apply {
                        put(download.request.id, download.state)
                    }

                    val progress = if (download.percentDownloaded == C.PERCENTAGE_UNSET.toFloat()) {
                        0f
                    } else {
                        download.percentDownloaded
                    }
                    _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                        put(download.request.id, progress)
                    }
                }
            }
        })
    }

    fun startDownload(video: Video) {
        val downloadRequest = DownloadRequest.Builder(video.id, video.url.toUri()).build()
        downloadManager.addDownload(downloadRequest)
    }

    fun pauseDownload(video: Video) {
        downloadManager.setStopReason(video.id, Download.STOP_REASON_NONE)
    }

    fun removeAllDownloads(video: Video) {
        downloadManager.removeAllDownloads()
    }

    fun resumeDownload(video: Video) {
        downloadManager.resumeDownloads()
    }

    fun removeDownload(video: Video) {
        downloadManager.removeDownload(video.id)
        viewModelScope.launch {
            _downloadStates.value = _downloadStates.value.toMutableMap().apply {
                remove(video.id)
            }
        }
    }

    fun getDownloadState(video: Video): Int {
        return _downloadStates.value[video.id] ?: Download.STATE_COMPLETED
    }
}