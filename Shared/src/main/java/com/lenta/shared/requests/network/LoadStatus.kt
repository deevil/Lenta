package com.lenta.shared.requests.network


sealed class LoadStatus

object NotInit : LoadStatus()
data class Loading (val startTime: Long, val loadingDataSize: Long): LoadStatus()
data class Loaded (val startTime: Long, val loadingDataSize: Long, val endTime: Long): LoadStatus()