package com.lenta.bp10.features.good_information.marked

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.StampMarkedCollector
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessMarkProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.PrintTaskNetRequest
import com.lenta.bp10.requests.network.SendWriteOffDataNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class MarkedInfoViewModel : BaseProductInfoViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sharedStringResourceManager: ISharedStringResourceManager

    @Inject
    lateinit var sendWriteOffDataNetRequest: SendWriteOffDataNetRequest

    @Inject
    lateinit var printTaskNetRequest: PrintTaskNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper


    private val processMarkProductService: ProcessMarkProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processMarkProduct(productInfo.value!!)!!
    }

    private val stampMarkedCollector: StampMarkedCollector by lazy {
        StampMarkedCollector(processMarkProductService)
    }

    var selectedPage = MutableLiveData(0)

    val properties by lazy {
        MutableLiveData(List(3) { index ->
            val position = index + 1
            ItemMarkedGoodPropertyUi(
                    position = "$position",
                    propertyName = "Параметр $position",
                    value = "Значение параметра $position"
            )
        }
        )
    }

    val rollBackEnabled: LiveData<Boolean> by lazy {
        countValue.map { it ?: 0.0 > 0.0 }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickRollBack() {
        stampMarkedCollector.rollback()
    }

    override fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        scanInfoResult?.let {
            if (it.productInfo.materialNumber == productInfo.value?.materialNumber) {
                return true
            }
        }
        onClickApply()
        return false
    }

    override fun getTaskDescription(): TaskDescription {
        return processMarkProductService.taskDescription
    }

    override fun getTaskRepo(): ITaskRepository {
        return processMarkProductService.taskRepository
    }

    override fun getProcessTotalCount(): Double {
        return processMarkProductService.getTotalCount()
    }

    override fun onClickAdd() {

    }

    override fun onClickApply() {

    }

    override fun initCountLiveData(): MutableLiveData<String> {
        return stampMarkedCollector.observeCount().map { it.toStringFormatted() }
    }

    override fun onBackPressed(): Boolean {
        if (stampMarkedCollector.isNotEmpty()) {
            screenNavigator.openConfirmationToBackNotEmptyStampsScreen {
                screenNavigator.goBack()
            }
            return false
        }
        processMarkProductService.discard()
        return true
    }

    override fun onScanResult(data: String) {

    }


}