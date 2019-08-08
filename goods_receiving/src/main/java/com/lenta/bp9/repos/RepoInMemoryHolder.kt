package com.lenta.bp9.repos

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.models.task.DirectSupplierTaskListRestInfo
import com.lenta.shared.requests.PermissionsResult

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsResult? = null
    override var tasksListRestInfo: MutableLiveData<DirectSupplierTaskListRestInfo> = MutableLiveData()

}

interface IRepoInMemoryHolder {
    var permissions: PermissionsResult?
    var tasksListRestInfo: MutableLiveData<DirectSupplierTaskListRestInfo>
}