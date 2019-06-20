package com.lenta.shared.account


data class SessionInfo(
        override var userName: String? = null,
        override var personnelNumber: String? = null,
        override var personnelFullName: String? = null,
        override var printer: String? = null,
        override var printerNumber: String? = null,
        override var market: String? = null,
        override var basicAuth: String? = null
) : ISessionInfo

interface ISessionInfo {
    var userName: String?
    var personnelNumber: String?
    var personnelFullName: String?
    var printer: String?
    var printerNumber: String?
    var market: String?
    var basicAuth: String?
}