package com.lenta.bp10

import com.lenta.shared.CoreApplication
import com.lenta.shared.settings.DefaultConnectionSettings

class AndroidApplication : CoreApplication() {
    override fun getDefaultConnectionSettings(): DefaultConnectionSettings {
        return DefaultConnectionSettings(
                serverAddress = "http://9.6.24.110",
                environment = "Lenta_LRQ",
                project = "PR_WOB"
        )
    }
}