package com.androidnomad.media

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download

@OptIn(UnstableApi::class)
@Composable
fun VideoListScreen(
    videoList: List<Video>,
    onVideoSelected: (Video) -> Unit,
    onDownloadRequested: (Video) -> Unit,
    onPauseRequested: (Video) -> Unit,
    onResumeRequested: (Video) -> Unit,
    onDelete: (Video) -> Unit,
    downloadState: Map<String, Int>,
    downloadProgress: Map<String, Float>
) {
    LazyColumn {
        items(videoList) { video ->
            VideoCard(
                video = video,
                onVideoSelected = onVideoSelected,
                onDownloadRequested = onDownloadRequested,
                onPauseRequested = onPauseRequested,
                onResumeRequested = onResumeRequested,
                onDelete = onDelete,
                downloadState = downloadState[video.id] ?: Download.FAILURE_REASON_NONE,
                downloadProgress = downloadProgress[video.id] ?: 0f
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoCard(
    video: Video,
    onVideoSelected: (Video) -> Unit,
    onDownloadRequested: (Video) -> Unit,
    onPauseRequested: (Video) -> Unit,
    onResumeRequested: (Video) -> Unit,
    onDelete: (Video) -> Unit,
    downloadState: Int,
    downloadProgress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onVideoSelected(video) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (downloadProgress > 0f && downloadProgress < 1f) {
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${(downloadProgress * 100).toInt()}%")
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onVideoSelected(video) }) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                when (downloadState) {
                    Download.STATE_DOWNLOADING -> {
                        IconButton(onClick = { onPauseRequested(video) }) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = "Pause",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

//                    Download.STATE_QUEUED -> {
//                        IconButton(onClick = { onPauseRequested(video) }) {
//                            Icon(
//                                Icons.Default.AccountBox,
//                                contentDescription = "Pause",
//                                modifier = Modifier.size(32.dp)
//                            )
//                        }
//                    }

                    Download.STATE_COMPLETED -> {
                        IconButton(onClick = { onDelete(video) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    else -> {
                        IconButton(onClick = { onDownloadRequested(video) }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Download",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
