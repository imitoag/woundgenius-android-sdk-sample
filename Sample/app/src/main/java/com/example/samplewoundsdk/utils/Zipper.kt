package com.example.samplewoundsdk.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


object Zipper {

    private const val BUFFER_SIZE = 8 * 1024

    fun zip(destination: File, vararg sources: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(destination))).use { out ->
            val data = ByteArray(BUFFER_SIZE)

            sources.forEach { source ->
                zipSingleSource(out, data, source)
            }
        }
    }

    private fun zipSingleSource(out: ZipOutputStream, data: ByteArray, source: File) {
        BufferedInputStream(FileInputStream(source), BUFFER_SIZE).use { origin ->
            val entry = ZipEntry(source.name)
            out.putNextEntry(entry)

            var count = origin.read(data, 0, BUFFER_SIZE)
            while (count != -1) {
                out.write(data, 0, count)
                count = origin.read(data, 0, BUFFER_SIZE)
            }
        }
    }

}