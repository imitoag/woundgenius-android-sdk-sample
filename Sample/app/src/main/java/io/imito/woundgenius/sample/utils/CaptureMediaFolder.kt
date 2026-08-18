package io.imito.woundgenius.sample.utils

import android.content.Context
import java.io.File

private const val CAPTURE_MEDIA_FOLDER = "captured_media"

/**
 * The folder this app hands the SDK to write captured media into.
 *
 * Deliberately under `filesDir` rather than `cacheDir`. The assessments kept in this app's database
 * outlive the capture that produced them, while a cache directory is disposable by definition: the
 * system evicts it under storage pressure and "Clear cache" in the app settings empties it on
 * demand. Media staged there disappears behind rows that still point at it, and the home screen is
 * left listing assessments with nothing to show.
 */
fun Context.captureMediaFolder(): File = File(filesDir, CAPTURE_MEDIA_FOLDER).apply { mkdirs() }
