package com.lenta.shared.utilities

import java.io.*

fun prepareFolder(path: String) {
    with(File(path)) {
        if (!exists()) {
            mkdirs().also {
                Logg.d { "mkDirs: $it" }
            }
        }
    }
}


fun writeToFile(data: String, filePath: String) {
    val file = File(filePath)
    try {
        val stream = FileOutputStream(file)
        stream.use {
            it.write(data.toByteArray())
        }

    } catch (e: java.lang.Exception) {
        Logg.e { "e: $e" }
    }


}

@Throws(Exception::class)
fun getStringFromFile(filePath: String): FileInfo {
    val file = File(filePath)
    if (file.exists() && file.isFile) {
        try {
            val fileInputStream = FileInputStream(file)
            val ret = convertStreamToString(fileInputStream)
            fileInputStream.close()
            return FileInfo(text = ret.trim(), lastModified = file.lastModified())
        } catch (e: java.lang.Exception) {
            Logg.e { "e: $e" }
        }
    }

    return FileInfo(text = "", lastModified = 0L)

}


@Throws(Exception::class)
private fun convertStreamToString(inputStream: InputStream): String {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val sb = StringBuilder()
    var line: String?
    while (reader.readLine().apply { line = this } != null) {
        sb.append(line).append("\n")
    }
    reader.close()
    return sb.toString()
}


data class FileInfo(
        val text: String,
        val lastModified: Long
)