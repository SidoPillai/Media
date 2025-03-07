package com.androidnomad.media

import android.app.Notification
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DefaultDownloaderFactory
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.scheduler.Scheduler
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

const val FOREGROUND_NOTIFICATION_UPDATE_INTERVAL = 1000L
const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"

/** A service for downloading media.  */
@UnstableApi
class MediaDownloadService @Inject constructor(
    val context: Context
) : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.download_notification_channel_name,
    0
) {
    companion object {
        private const val JOB_ID = 1
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }

    private var downloadNotificationHelper: DownloadNotificationHelper? = null

    val downloadMgr = getDownloadManager()

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = getDownloadManagerInstance(context)
        val downloadNotificationHelper = getDownloadNotificationHelper(context.applicationContext) // ✅ FIXED
        downloadManager.addListener(
            TerminalStateNotificationHelper(
                context.applicationContext, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1
            )
        )
        return downloadManager
    }

    public fun requestDownload(video: Video) {
        val request = DownloadRequest.Builder(video.id, video.url.toUri())
            .build()
        sendAddDownload(context, this::class.java, request, true);
    }

    @OptIn(UnstableApi::class)
    private fun getDownloadManagerInstance(context: Context): DownloadManager {
        val databaseProvider = StandaloneDatabaseProvider(context)
        val downloadIndex = DefaultDownloadIndex(databaseProvider)

        // Create a cache for storing downloads
        val cache = SimpleCache(
            File(context.cacheDir, "downloads"),
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024), // 100MB cache limit
            databaseProvider
        )

        val upstreamFactory: DataSource.Factory = DefaultHttpDataSource.Factory()

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null) // Use default data sink
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // Create a DownloaderFactory (supports HLS, DASH, Progressive)
        val downloaderFactory = DefaultDownloaderFactory(cacheDataSourceFactory, Executors.newFixedThreadPool(2))

        return DownloadManager(context, downloadIndex, downloaderFactory).apply {
            resumeDownloads()
        };
    }

    override fun getScheduler(): Scheduler {
        return PlatformScheduler(this, JOB_ID)
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: @Requirements.RequirementFlags Int
    ): Notification {
        return getDownloadNotificationHelper(/* context= */ this)
            .buildProgressNotification(
                /* context= */ this,
                R.drawable.ic_download,
                /* contentIntent= */ null,
                /* message= */ null,
                downloads,
                notMetRequirements
            )
    }

    @Synchronized
    fun getDownloadNotificationHelper(context: Context): DownloadNotificationHelper {
        if (downloadNotificationHelper == null) {
            downloadNotificationHelper = DownloadNotificationHelper(context.applicationContext, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        }
        return downloadNotificationHelper as DownloadNotificationHelper
    }

    private inner class TerminalStateNotificationHelper(
        context: Context,
        private val notificationHelper: DownloadNotificationHelper,
        firstNotificationId: Int
    ) : DownloadManager.Listener {

        private val context: Context = context.applicationContext
        private var nextNotificationId: Int = firstNotificationId

        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            val notification: Notification = when (download.state) {
                Download.STATE_COMPLETED -> notificationHelper.buildDownloadCompletedNotification(
                    context,
                    R.drawable.ic_download,
                    /* contentIntent= */ null,
                    Util.fromUtf8Bytes(download.request.data)
                )

                Download.STATE_FAILED -> notificationHelper.buildDownloadFailedNotification(
                    context,
                    R.drawable.ic_download,
                    /* contentIntent= */ null,
                    Util.fromUtf8Bytes(download.request.data)
                )

                else -> return
            }
            NotificationUtil.setNotification(context, nextNotificationId++, notification)
        }
    }
}