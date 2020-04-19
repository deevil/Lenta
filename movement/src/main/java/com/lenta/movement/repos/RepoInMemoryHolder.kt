package com.lenta.movement.repos

import com.lenta.movement.requests.network.StockLockRequestResult
import com.lenta.shared.requests.network.StoresRequestResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var storesRequestResult: StoresRequestResult? = null
    override var stockLockRequestResult: StockLockRequestResult? = null
}

interface IRepoInMemoryHolder {
    var storesRequestResult: StoresRequestResult?
    var stockLockRequestResult: StockLockRequestResult?
}