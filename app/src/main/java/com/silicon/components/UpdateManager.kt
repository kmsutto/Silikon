package com.silicon.ui.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import com.silicon.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

@Keep
object UpdateManager {

    @Keep
    data class UpdateInfo(
        val version: String,
        val changelog: String,
        val downloadUrl: String
    )

    @Keep
    sealed class UpdateState {
        object Checking : UpdateState()
        object UpToDate : UpdateState()
        data class Available(val info: UpdateInfo) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    private const val GITHUB_OWNER = "kmsutto"
    private const val GITHUB_REPO = "Silikon"

    suspend fun checkForUpdates(): UpdateState {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
                connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("User-Agent", "Silicon-App")
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

                val responseCode = connection.responseCode

                if (responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonObject = JSONObject(response.toString())
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

                    if (isNewVersionGreater(currentVersionClean, remoteVersionClean)) {
                        UpdateState.Available(UpdateInfo(tagName, body, downloadUrl))
                    } else {
                        UpdateState.UpToDate
                    }

                } else {
                    throw Exception("HTTP $responseCode")
                }

            } catch (e: Exception) {
                Log.e("UpdateManager", "Check failed: ${e.message}")
                UpdateState.Error(e.localizedMessage ?: "Check failed")
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun isNewVersionGreater(current: String, remote: String): Boolean {
        try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
            val length = max(currentParts.size, remoteParts.size)
            for (i in 0 until length) {
                val c = currentParts.getOrElse(i) { 0 }
                val r = remoteParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        } catch (_: Exception) { return false }
        return false
    }

    fun downloadAndInstall(context: Context, url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading update...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        installApk(ctxt, fileName)
                        try { ctxt.unregisterReceiver(this) } catch (_: Exception) {}
                    }
                }
            }
            val flags = if (Build.VERSION.SDK_INT >= 34) Context.RECEIVER_EXPORTED else 0
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), flags)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Download failed", e)
        }
    }

    private fun installApk(context: Context, fileName: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}