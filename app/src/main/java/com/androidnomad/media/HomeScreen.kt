package com.androidnomad.media

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    viewModel: DownloadViewModel = hiltViewModel(),
    onVideoSelected: (Video) -> Unit
) {
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadState by viewModel.downloadStates.collectAsState()

    LaunchedEffect(downloadState) {
        downloadState.keys.forEach {
            Log.d("HomeScreen", "Download Status: ${downloadState[it] ?: "Not Started"}")
        }
    }

    VideoListScreen(
        videoList = videoList,
        onVideoSelected = onVideoSelected,
        onDownloadRequested = {
            viewModel.startDownload(it)
        },
        onPauseRequested = {
            viewModel.pauseDownload(it)
        },
        onResumeRequested = {
            viewModel.resumeDownload(it)
        },
        onDelete = {
            viewModel.removeDownload(it)
        },
        downloadState = downloadState,
        downloadProgress = downloadProgress
    )
}