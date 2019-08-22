package com.lenta.bp9.repos

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.TaskList
import com.lenta.bp9.requests.network.PermissionsGrzResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsGrzResult? = null
    override var taskList: MutableLiveData<TaskList> = MutableLiveData()
    override var lastSearchResult: MutableLiveData<TaskList> = MutableLiveData()
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsGrzResult?
    var taskList: MutableLiveData<TaskList>
    var lastSearchResult: MutableLiveData<TaskList>
}