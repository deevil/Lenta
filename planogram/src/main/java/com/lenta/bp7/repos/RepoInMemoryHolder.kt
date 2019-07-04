package com.lenta.bp7.repos

import com.lenta.bp7.requests.network.PermissionsResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissionsResult: PermissionsResult? = null
}

interface IRepoInMemoryHolder {
    var permissionsResult: PermissionsResult?
}