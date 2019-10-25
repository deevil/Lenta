package com.lenta.shared.account

import androidx.lifecycle.MutableLiveData


data class SessionInfo(
        override var packageName: String? = null,
        override var userName: String? = null,
        override var personnelNumber: String? = null,
        override var personnelFullName: String? = null,
        override var printer: String? = null,
        override var printerNumber: String? = null,
        override var market: String? = null,
        override var basicAuth: String? = null,
        override var isAuthSkipped: MutableLiveData<Boolean> = MutableLiveData(false)
) : ISessionInfo

interface ISessionInfo {
    var packageName: String?
    var userName: String?
    var personnelNumber: String?
    var personnelFullName: String?
    var printer: String?
    var printerNumber: String?
    var market: String?
    var basicAuth: String?
    var isAuthSkipped: MutableLiveData<Boolean>
}