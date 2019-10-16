package com.lenta.bp14.repos

import com.lenta.bp14.requests.user_permitions.PermissionsRequestResult
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