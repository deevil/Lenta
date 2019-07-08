package com.lenta.inventory.repos

import com.lenta.inventory.requests.network.PermissionsResult
import com.lenta.inventory.requests.network.TasksListRestInfo

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissionsResult: PermissionsResult? = null
    override var tasksListRestInfo: TasksListRestInfo? = null
}

interface IRepoInMemoryHolder {
    var permissionsResult: PermissionsResult?
    var tasksListRestInfo: TasksListRestInfo?
}