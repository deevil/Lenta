package com.lenta.bp15.repository

import com.lenta.bp15.request.PermissionsRequestResult
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