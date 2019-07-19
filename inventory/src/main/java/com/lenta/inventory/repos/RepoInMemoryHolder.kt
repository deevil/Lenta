package com.lenta.inventory.repos

import androidx.lifecycle.MutableLiveData
import com.lenta.inventory.requests.network.PermissionsResult
import com.lenta.inventory.requests.network.TaskContentRestInfo
import com.lenta.inventory.requests.network.TasksListRestInfo

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
    override var tasksListRestInfo: MutableLiveData<TasksListRestInfo> = MutableLiveData()
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var tasksListRestInfo: MutableLiveData<TasksListRestInfo>
}