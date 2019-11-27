package com.lenta.bp16.repository

import com.lenta.bp16.request.PermissionsRequestResult
import com.lenta.shared.requests.PermissionsResult
import javax.inject.Inject

class RepoInMemoryHolder @Inject constructor() : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
    override var storesRequestResult: PermissionsRequestResult? = null
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var storesRequestResult: PermissionsRequestResult?
}