package com.lenta.bp9.features.search_task

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.TaskListSearchParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class SearchTaskViewModel: CoreViewModel() {

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    var taskNumber: MutableLiveData<String> = MutableLiveData("")
    var supplier: MutableLiveData<String> = MutableLiveData("")
    var order: MutableLiveData<String> = MutableLiveData("")
    var invoice: MutableLiveData<String> = MutableLiveData("")
    var transportation: MutableLiveData<String> = MutableLiveData("")
    var numberGE: MutableLiveData<String> = MutableLiveData("")
    var numberEO: MutableLiveData<String> = MutableLiveData("")

    val searchEnabled: MutableLiveData<Boolean> = combineLatest(combineLatest(taskNumber, supplier, order), combineLatest(invoice, transportation, numberGE), numberEO).map {
        it?.first?.first?.isNotEmpty() ?: false ||
                it?.first?.second?.isNotEmpty() ?: false ||
                it?.first?.third?.isNotEmpty() ?: false ||
                it?.second?.first?.isNotEmpty() ?: false ||
                it?.second?.second?.isNotEmpty() ?: false ||
                it?.second?.third?.isNotEmpty() ?: false ||
                it?.third?.isNotEmpty() ?: false
    }

    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }

    fun init() {

    }

    fun onClickFind() {
        screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.Receiving,
                TaskListSearchParams(taskNumber = if (taskNumber.value.isNullOrEmpty()) null else taskNumber.value,
                supplierNumber = if (supplier.value.isNullOrEmpty()) null else supplier.value,
                documentNumber = if (order.value.isNullOrEmpty()) null else order.value,
                invoiceNumber = if (invoice.value.isNullOrEmpty()) null else invoice.value,
                transportNumber = if (transportation.value.isNullOrEmpty()) null else transportation.value,
                numberGE = if (numberGE.value.isNullOrEmpty()) null else numberGE.value,
                numberEO = if (numberEO.value.isNullOrEmpty()) null else numberEO.value)
        )
    }
}