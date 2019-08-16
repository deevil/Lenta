package com.lenta.shared.analytics.db

import com.lenta.shared.platform.constants.Constants.TIME_FORMAT_LOGS
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.prepareFolder
import java.io.File
import java.lang.Exception

class FileArchivator(private val filePath: String,
                     private val archivePath: String,
                     private val sizeLimitInKb: Int = 1024,
                     private val archivesCountLimit: Int = 10) {

    fun backup(): Boolean {

        val file = File(filePath)

        if (!file.exists()) {
            return false
        }

        val fileSizeInKb = file.length() / 1024

        Logg.d { "fileSizeInKb: $fileSizeInKb" }

        if (fileSizeInKb > sizeLimitInKb) {
            return createBackup(file)
        }

        return false

    }

    private fun createBackup(file: File) : Boolean {
        prepareFolder(archivePath)
        try {
            val backupFile = File("$archivePath/${getArchiveFileName(file)}")
            Logg.d { "backupFile :${backupFile.absolutePath}" }
            file.copyTo(backupFile)
            checkArchivesFiles()
            return true
        } catch (exception: Exception) {
            Logg.e { "createBackup exception: $exception" }
            return false
        }

    }

    private fun checkArchivesFiles() {

        val archiveFiles = File(archivePath).listFiles()

        if (archiveFiles.size > archivesCountLimit) {
            archiveFiles.sortBy { it.name }
            archiveFiles[0].delete()
        }

    }

    private fun getArchiveFileName(file: File): String {
        file.name.split(".").let { parts ->
            return parts.mapIndexed { index, part ->
                if (index == parts.size - 2) {
                    "${part}_archive(${DateTimeUtil.formatCurrentDate(TIME_FORMAT_LOGS)})"
                } else {
                    part
                }
            }.joinToString(".")
        }
    }

}