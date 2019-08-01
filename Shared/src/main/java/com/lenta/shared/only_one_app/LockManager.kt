package com.lenta.shared.only_one_app

import android.content.Context
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.coroutine.timer
import com.lenta.shared.utilities.getStringFromFile
import com.lenta.shared.utilities.writeToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockManager(val context: Context) {

    private val lockFilePath = "${Constants.DB_PATH}/lock_file"

    private var needLock = false

    private val packageName by lazy {
        context.packageName
    }

    init {
        GlobalScope.launch {
            timer(1000) {
                if (needLock) {
                    Logg.d { "Lock file" }
                    withContext(Dispatchers.IO) {
                        writeToFile(packageName, lockFilePath)
                    }
                }

            }
        }
    }

    fun lock() {
        needLock = true
    }

    fun unlock() {
        needLock = false
    }


    fun getActiveAppPackageName(): String? {
        val fileInfo = getStringFromFile(lockFilePath)
        if (fileInfo.lastModified == 0L) {
            return null
        }

        return if (fileInfo.text.isNotBlank() && fileInfo.text != packageName && fileModifiedNow(fileInfo.lastModified)) {
            fileInfo.text
        } else {
            null
        }


    }

    private fun fileModifiedNow(lastModified: Long): Boolean {
        (System.currentTimeMillis() - lastModified).let {
            return it < 2000L
        }
    }

}