package com.lenta.inventory.repos

import com.lenta.inventory.requests.network.PermissionsResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissionsResult: PermissionsResult? = null
}

interface IRepoInMemoryHolder {
    var permissionsResult: PermissionsResult?
}