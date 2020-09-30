package com.lenta.bp10.repos

import com.lenta.bp10.requests.network.StockLockRequestResult
import com.lenta.bp10.requests.network.UserResourcesResult
import com.lenta.shared.requests.network.StoresRequestResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var storesRequestResult: StoresRequestResult? = null
    override var stockLockRequestResult: StockLockRequestResult? = null
    override var userResourceResult: UserResourcesResult? = null
}

interface IRepoInMemoryHolder {
    var storesRequestResult: StoresRequestResult?
    var stockLockRequestResult: StockLockRequestResult?
    var userResourceResult: UserResourcesResult?
}