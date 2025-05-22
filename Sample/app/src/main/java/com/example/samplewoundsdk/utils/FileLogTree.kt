package com.example.samplewoundsdk.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.example.samplewoundsdk.R
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class FileLogTree(val context: Context) : Timber.Tree() {

    private val writeFile = AtomicReference<File>()
    private val mutex = Mutex()

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + createJob())
    private fun createJob(): Job = SupervisorJob()

    private fun priorityConvert(priority: Int): String {
        return when (priority) {
            Log.VERBOSE -> "VERBOSE"
            Log.DEBUG -> "DEBUG"
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "UNKNOWS"
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.DEBUG) {
            return
        }
        try {
            writeLogToFile(
                "${convertLongToTime(System.currentTimeMillis())} ${
                    priorityConvert(
                        priority
                    )
                }, $tag, $message ${t?.stackTrace?.joinToString { it.toString() } ?: ""}\n")
        } catch (e: IOException) {
            writeFile.set(null)
            Timber.e("Error while logging into file: $e")
        }
    }

    fun clearOldLogs() {
        val logsFolder = File(context.cacheDir, FOLDER_TIMBER_LOGS)
        val files = logsFolder.listFiles()
        files?.forEach { file ->
            val date = if (file.name.startsWith("tl")) {
                convertTimeToDate(file.name.drop(2))
            } else {
                Date(file.lastModified())
            }
            val calendarWeekAgo = Calendar.getInstance().apply {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) - 1)
            }
            if (date == null || date.before(calendarWeekAgo.time)) {
                file.delete()
            }
        }
    }

    private fun writeLogToFile(log: String) =
        coroutineScope.launch {
            try {
                if (writeFile.get() == null) {
                    writeFile.lazySet(context.createFile(fileName = "tl${convertLongToTime(System.currentTimeMillis())}.txt"))
                }
                writeToLogFile(log)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

    private suspend fun writeToLogFile(log: String) {
        mutex.withLock {
            val result = runCatching {
                writeFile.get().appendText(log)
            }
            if (result.isFailure) {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    private fun convertLongToTime(long: Long): String {
        val date = Date(long)
        val format = SimpleDateFormat(ANDROID_LOG_TIME_FORMAT, Locale.US)
        return format.format(date)
    }

    private fun convertTimeToDate(time: String): Date? {
        val format = SimpleDateFormat(ANDROID_LOG_TIME_FORMAT, Locale.US)
        return format.parse(time)
    }

    @SuppressLint("NewApi")
    private fun Context.createFile(fileName: String): File {
        val folder = File(context.cacheDir, FOLDER_TIMBER_LOGS)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder, fileName).apply {
            if (!exists()) {
                createNewFile()
            }
        }
    }

    companion object {
        private const val ANDROID_LOG_TIME_FORMAT = "MM-dd-yy_HH_mm_ss.SSS"
        private const val FOLDER_TIMBER_LOGS = "current_session_logs"
        private const val FILE_TIMBER_LOGS = "current_session.zip"
        private const val MIME_TYPE_PDF = "application/pdf"

        private fun Context.sendEmailWithFile(
            email: String,
            subject: String? = null,
            message: String? = null,
            attachment: File? = null
        ): Boolean {
            if (attachment != null) {
                if (!attachment.exists() || !attachment.canRead()) {
                    return false
                }
            }

            return sendEmail(email, subject, message, attachment?.uri(this))
        }

        private fun File.uri(context: Context): Uri {
            return FileProvider.getUriForFile(
                context,
                context.getString(R.string.file_provider),
                this
            )
        }

        private fun Context.sendEmail(
            email: String,
            subject: String? = null,
            message: String? = null,
            attachment: Uri? = null
        ): Boolean {
//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "text/plain"
//                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
//                subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
//                subject?.let { putExtra(Intent.EXTRA_TEXT, message) }
//                attachment?.let { putExtra(Intent.EXTRA_STREAM, it) }
//                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//            }
//
//            try {
//                startActivity(Intent.createChooser(intent, "Send Email"))
//            } catch (e: ActivityNotFoundException) {
//                return false
//            }
            try {
                attachment?.let {
                    ShareCompat.IntentBuilder(this)
                        .setType(MIME_TYPE_PDF)
                        .setStream(
                            attachment
                        ).createChooserIntent().apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(this)
                        }
                }
            } catch (e: Exception) {
                return false
            }

            return true
        }

        fun shareLogs(context: Context) {
            try {
                val sources =
                    File(context.cacheDir, FOLDER_TIMBER_LOGS).listFiles() ?: emptyArray()
                val destination = File(context.cacheDir, FILE_TIMBER_LOGS)
                if (destination.exists()) {
                    destination.delete()
                }
                destination.createNewFile()

                Zipper.zip(destination, *sources)

                context.sendEmailWithFile(
                    email = "",
                    subject = "LOGS",
                    message = "Please send this file to the developers",
                    attachment = destination
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}