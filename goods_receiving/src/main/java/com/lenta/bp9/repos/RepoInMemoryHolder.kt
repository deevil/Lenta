package com.lenta.bp9.repos

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.models.task.TaskList
import com.lenta.shared.requests.PermissionsResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
    override var taskList: MutableLiveData<TaskList> = MutableLiveData()
    override var lastSearchResult: MutableLiveData<TaskList> = MutableLiveData()
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var taskList: MutableLiveData<TaskList>
    var lastSearchResult: MutableLiveData<TaskList>
}