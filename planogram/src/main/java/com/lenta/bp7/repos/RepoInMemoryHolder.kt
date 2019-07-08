package com.lenta.bp7.repos

import com.lenta.bp7.requests.network.SettingRequestResult
import com.lenta.bp7.requests.network.StoresRequestResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var storesRequestResult: StoresRequestResult? = null
    override var settingRequestResult: SettingRequestResult? = null
}

interface IRepoInMemoryHolder {
    var storesRequestResult: StoresRequestResult?
    var settingRequestResult: SettingRequestResult?
}