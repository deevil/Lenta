package com.lenta.bp12.features.open_task.task_search

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.actionByNumber
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.pojo.TaskSearchParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class TaskSearchViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: IOpenTaskManager

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
    }

    val requestFocusToProvider = MutableLiveData(false)

    val provider = MutableLiveData("")

    val goodNumber = MutableLiveData("")

    val exciseNumber = MutableLiveData("")

    val section = MutableLiveData("")

    val searchEnabled = provider.combineLatest(goodNumber).combineLatest(exciseNumber).combineLatest(section).map {
        it?.let {
            val provider = it.first.first.first
            val good = it.first.first.second
            val excise = it.first.second
            val section = it.second

            provider.isNotEmpty() || good.isNotEmpty() || excise.isNotEmpty() || section.isNotEmpty()
        }
    }

    // -----------------------------

    fun onClickSearch() {
        manager.searchParams = TaskSearchParams(
                providerCode = provider.value.orEmpty(),
                goodNumber = goodNumber.value.orEmpty(),
                exciseNumber = exciseNumber.value.orEmpty(),
                section = section.value.orEmpty()
        )

        manager.isNeedLoadTaskListByParams = true
        navigator.goBack()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (searchEnabled.value == true) {
            onClickSearch()
        } else {
            requestFocusToProvider.value = true
        }

        return true
    }

    fun onScanResult(data: String) {
        checkSearchNumber(data)
    }

    private fun checkSearchNumber(number: String) {
        actionByNumber(
                number = number,
                funcForEan = ::fillGoodNumberField,
                funcForMaterial = ::fillGoodNumberField,
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForExcise = ::fillExciseNumberField,
                funcForNotValidBarFormat = navigator::showIncorrectEanFormat
        )
    }

    private fun fillGoodNumberField(number: String) {
        goodNumber.value = number
    }

    private fun fillExciseNumberField(number: String) {
        exciseNumber.value = number
    }

}
