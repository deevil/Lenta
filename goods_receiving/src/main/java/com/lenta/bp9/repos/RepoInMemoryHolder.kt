package com.lenta.bp9.repos

import com.lenta.shared.requests.PermissionsResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
}