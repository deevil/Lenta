package com.lenta.bp14.repos

import com.lenta.shared.requests.PermissionsResult
import com.lenta.shared.requests.network.StoresRequestResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
    override var storesRequestResult: StoresRequestResult? = null
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var storesRequestResult: StoresRequestResult?
}