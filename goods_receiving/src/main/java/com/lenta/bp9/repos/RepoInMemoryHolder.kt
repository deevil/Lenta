package com.lenta.bp9.repos

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.InvoiceContentEntry
import com.lenta.bp9.requests.network.PermissionsGrzResult
import com.lenta.shared.models.core.Manufacturer

class RepoInMemoryHolder : IRepoInMemoryHolder {
    override var permissions: PermissionsGrzResult? = null
    override var taskList: MutableLiveData<TaskList> = MutableLiveData()
    override var lastSearchResult: MutableLiveData<TaskList> = MutableLiveData()
    override var invoiceContents: MutableLiveData<List<InvoiceContentEntry>> = MutableLiveData()
    override var manufacturers: MutableLiveData<List<Manufacturer>> = MutableLiveData()
    override var processOrderData: MutableLiveData<List<TaskProcessOrderDataInfo>> = MutableLiveData()
    override var sets: MutableLiveData<List<TaskSetsInfo>> = MutableLiveData()
    override var markingGoodsProperties: MutableLiveData<List<TaskMarkingGoodsProperties>> = MutableLiveData()
    override var manufacturersForZBatches: MutableLiveData<List<TaskManufacturersForZBatches>> = MutableLiveData()
    override var taskZBatchInfo: MutableLiveData<List<TaskZBatchInfo>> = MutableLiveData()
}

interface IRepoInMemoryHolder {
    var permissions: PermissionsGrzResult?
    var taskList: MutableLiveData<TaskList>
    var lastSearchResult: MutableLiveData<TaskList>
    var invoiceContents: MutableLiveData<List<InvoiceContentEntry>>
    var manufacturers: MutableLiveData<List<Manufacturer>>
    var processOrderData: MutableLiveData<List<TaskProcessOrderDataInfo>>
    var sets: MutableLiveData<List<TaskSetsInfo>>
    var markingGoodsProperties: MutableLiveData<List<TaskMarkingGoodsProperties>>
    var manufacturersForZBatches: MutableLiveData<List<TaskManufacturersForZBatches>>
    var taskZBatchInfo: MutableLiveData<List<TaskZBatchInfo>>
}

fun IRepoInMemoryHolder.findManufacturerNameByCodeOrEGAIS(manufacturerCode: String, organizationCodeEGAIS: String?): String {
    return this.manufacturers.value
            ?.findLast { it.code == manufacturerCode }
            ?.name
            ?: this.manufacturers.value
                    ?.findLast { manufacture -> //это в случае излишка
                        manufacture.code == organizationCodeEGAIS
                    }
                    ?.name
                    .orEmpty()
}