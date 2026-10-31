package com.example

import android.app.Application
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KrasTvApplication : Application() {

    companion object {
        private const val CRASH_FILE_NAME = "kras_last_crash.txt"

        fun getSavedCrash(context: Context): String? {
            return try {
                val file = File(context.filesDir, CRASH_FILE_NAME)
                if (file.exists()) {
                    val content = file.readText()
                    if (content.isNotBlank()) content else null
                } else null
            } catch (_: Exception) {
                null
            }
        }

        fun clearSavedCrash(context: Context) {
            try {
                val file = File(context.filesDir, CRASH_FILE_NAME)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Global Uncaught Exception Handler
        setupGlobalExceptionHandler()

        // 2. Configure Coil with Strict Memory Limits for fast large-catalog scrolling
        setupCoilImageLoader()
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()

                val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val crashReport = buildString {
                    append("Timestamp: ").append(timeStamp).append("\n")
                    append("Thread: ").append(thread.name).append("\n")
                    append("Exception: ").append(throwable.javaClass.name).append("\n")
                    append("Message: ").append(throwable.localizedMessage ?: "Unknown").append("\n\n")
                    append("Stack Trace:\n").append(stackTrace)
                }

                val file = File(filesDir, CRASH_FILE_NAME)
                file.writeText(crashReport)
            } catch (_: Exception) {
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun setupCoilImageLoader() {
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15) // Limit memory cache to 15% of available app memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("kras_image_cache"))
                    .maxSizeBytes(60L * 1024 * 1024) // 60 MB disk cache
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()

        Coil.setImageLoader(imageLoader)
    }
}
