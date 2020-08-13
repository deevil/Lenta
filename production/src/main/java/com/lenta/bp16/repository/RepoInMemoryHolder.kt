package com.lenta.bp16.repository

import com.lenta.bp16.request.PermissionsRequestResult
import com.lenta.bp16.request.StockLockRequestResult
import com.lenta.shared.requests.PermissionsResult
import javax.inject.Inject

class RepoInMemoryHolder @Inject constructor() : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
    override var storesRequestResult: PermissionsRequestResult? = null
    override var stockLockRequestResult: StockLockRequestResult? = null
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var storesRequestResult: PermissionsRequestResult?
    var stockLockRequestResult: StockLockRequestResult?
}