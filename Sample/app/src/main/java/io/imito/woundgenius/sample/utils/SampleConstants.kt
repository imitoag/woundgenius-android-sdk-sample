package io.imito.woundgenius.sample.utils

/**
 * Constants the sample used to import from the SDK's internal `Constants` object.
 *
 * As of SDK 1.6.1 that object is no longer part of the published artifact (R8 strips the
 * `internal.utils.keys` package), and several of these values were dropped from the SDK
 * altogether, so the sample keeps its own copies.
 */
object SampleConstants {

    const val SERVER_DATE_PATTERN = "yyyy-MM-dd"
    const val SERVER_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
    const val UTC_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss"

    const val FORMS_FOLDER = "forms"
    const val MIME_TYPE_JSON = "application/json"

    val supportedVideoExtensions = listOf(
        ".mp4", ".mkv", ".webm", ".m4v", ".mov", ".flv", ".ts",
        ".m3u8", ".mpd",
        ".mp3", ".aac", ".ogg", ".wav", ".flac", ".amr"
    )
}
