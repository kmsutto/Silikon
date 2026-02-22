package com.silicon.ui.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import com.silicon.BuildConfig

@Keep
object UpdateManager {

    @Keep
    data class UpdateInfo(val version: String, val changelog: String, val downloadUrl: String)

    @Keep
    sealed class UpdateState {
        object Checking : UpdateState()
        object UpToDate : UpdateState()
        data class Available(val info: UpdateInfo) : UpdateState()
        data class Tester(val currentVersion: String) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    private const val GITHUB_OWNER = "kmsutto"
    private const val GITHUB_REPO = "Silikon"

    suspend fun checkForUpdates(): UpdateState = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
            connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "Silicon-App")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            if (connection.responseCode == 200) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonObject = JSONObject(response)
                val tagName = jsonObject.getString("tag_name")
                val body = jsonObject.optString("body", "No description")
                val assets = jsonObject.getJSONArray("assets")

                var downloadUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                if (downloadUrl.isEmpty()) throw Exception("No APK in release")

                val remoteVersionClean = tagName.replace(Regex("[^0-9.]"), "")
                val currentVersionClean = BuildConfig.VERSION_NAME.replace(Regex("[^0-9.]"), "")
                val comparisonResult = compareVersions(currentVersionClean, remoteVersionClean)

                when {
                    comparisonResult < 0 -> UpdateState.Available(UpdateInfo(tagName, body, downloadUrl))
                    comparisonResult > 0 -> UpdateState.Tester(BuildConfig.VERSION_NAME)
                    else -> UpdateState.UpToDate
                }
            } else {
                throw Exception("HTTP ${connection.responseCode}")
            }
        } catch (e: Exception) {
            UpdateState.Error(e.localizedMessage ?: "Check failed")
        } finally {
            connection?.disconnect()
        }
    }

    private fun compareVersions(current: String, remote: String): Int {
        try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
            val length = max(currentParts.size, remoteParts.size)
            for (i in 0 until length) {
                val c = currentParts.getOrElse(i) { 0 }
                val r = remoteParts.getOrElse(i) { 0 }
                if (c > r) return 1
                if (c < r) return -1
            }
            return 0
        } catch (_: Exception) { return 0 }
    }

    fun downloadAndInstall(context: Context, url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("Downloading update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                        installApk(ctxt, fileName)
                        try { ctxt.unregisterReceiver(this) } catch (_: Exception) {}
                    }
                }
            }
            val flags = if (Build.VERSION.SDK_INT >= 34) Context.RECEIVER_EXPORTED else 0
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), flags)
        } catch (_: Exception) { }
    }

    private fun installApk(context: Context, fileName: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}