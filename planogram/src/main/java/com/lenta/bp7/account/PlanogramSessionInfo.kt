package com.lenta.bp7.account

import com.lenta.bp7.data.model.CheckStoreData

data class PlanogramSessionInfo(
        override var userName: String? = null,
        override var personnelNumber: String? = null,
        override var personnelFullName: String? = null,
        override var printer: String? = null,
        override var printerNumber: String? = null,
        override var marketNumber: String = "",
        override var basicAuth: String? = null,
        override var checkType: String? = null,
        override var checkStoreData: CheckStoreData = CheckStoreData()
) : IPlanogramSessionInfo

interface IPlanogramSessionInfo {
    var userName: String?
    var personnelNumber: String?
    var personnelFullName: String?
    var printer: String?
    var printerNumber: String?
    var marketNumber: String
    var basicAuth: String?
    var checkType: String?
    var checkStoreData: CheckStoreData
}