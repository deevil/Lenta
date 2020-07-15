package com.lenta.bp18.repository

import com.lenta.bp18.request.PermissionsRequestResult
import com.lenta.shared.requests.PermissionsResult
import com.lenta.shared.requests.network.StoresRequestResult
import javax.inject.Inject

class RepoInMemoryHolder @Inject constructor() : IRepoInMemoryHolder {

    override var storesRequestResult: StoresRequestResult? = null
}

interface IRepoInMemoryHolder {
    var storesRequestResult: StoresRequestResult?
}