package com.lenta.shared.platform.statusbar

import androidx.lifecycle.MutableLiveData

data class StatusBarUiModel(
        val pageNumber: MutableLiveData<String> = MutableLiveData(""),
        var ip: MutableLiveData<String> = MutableLiveData(""),
        val printerTasksCount: MutableLiveData<Int> = MutableLiveData(0),
        val batteryLevel: MutableLiveData<Int> = MutableLiveData(0),
        val time: MutableLiveData<String> = MutableLiveData(""),
        val networkConnected: MutableLiveData<Boolean> = MutableLiveData(false)
)