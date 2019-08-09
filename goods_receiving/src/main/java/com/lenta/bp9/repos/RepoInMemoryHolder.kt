package com.lenta.bp9.repos

import com.lenta.bp9.models.task.TaskList
import com.lenta.shared.requests.PermissionsResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
    override var taskList: TaskList? = null
    override var lastSearchResult: TaskList? = null
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var taskList: TaskList?
    var lastSearchResult: TaskList?
}