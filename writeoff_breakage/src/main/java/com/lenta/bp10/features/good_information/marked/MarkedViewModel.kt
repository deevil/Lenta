package com.lenta.bp10.features.good_information.marked

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.PrintTaskNetRequest
import com.lenta.bp10.requests.network.SendWriteOffDataNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class MarkedViewModel : BaseProductInfoViewModel(), PageSelectionListener {

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


    var selectedPage = MutableLiveData(0)

    val properties by lazy {
        MutableLiveData(List(3) { index ->
            ItemMarkedGoodPropertyUi(
                    position = "${index + 1}",
                    propertyName = "Параметр",
                    value = "Значение параметра"
            )
        }
        )
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getTaskDescription(): TaskDescription {
        TODO("Not yet implemented")
    }

    override fun getTaskRepo(): ITaskRepository {
        TODO("Not yet implemented")
    }

    override fun getProcessTotalCount(): Double {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

    override fun onClickApply() {
        TODO("Not yet implemented")
    }

    override fun initCountLiveData(): MutableLiveData<String> {
        TODO("Not yet implemented")
    }

    override fun onBackPressed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScanResult(data: String) {
        TODO("Not yet implemented")
    }


}