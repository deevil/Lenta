package com.lenta.bp15.repository

import com.lenta.bp15.repository.requests.pojo.UserPermissionsResult
import com.lenta.shared.requests.PermissionsResult
import javax.inject.Inject

class RepoInMemoryHolder @Inject constructor() : IRepoInMemoryHolder {

    override var permissions: PermissionsResult? = null
    override var storesRequestResult: UserPermissionsResult? = null

}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var storesRequestResult: UserPermissionsResult?
}