package com.lenta.bp16.data

import com.google.gson.Gson
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.settings.AppSettings
import javax.inject.Inject

class Scales @Inject constructor(
        private val appSettings: AppSettings,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : IScales {

    override fun getWeight(): Int {
        return 0
    }

}

interface IScales {
    fun getWeight() : Int
}