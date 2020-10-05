package com.lenta.bp15.features.search_task

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.model.enum.ShoesMarkType
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.bp15.repository.requests.pojo.TaskSearchParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class SearchTaskViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager

    @Inject
    lateinit var manager: ITaskManager


    /**
    Переменные
     */

    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
    }

    val number = MutableLiveData("")

    val requestFocusToSearch = MutableLiveData(false)

    val isMan = MutableLiveData(false)

    val isWoman = MutableLiveData(false)

    val isChildren = MutableLiveData(false)

    val isUnisex = MutableLiveData(false)

    val searchEnabled = number.map { it?.isNotEmpty() }

    /**
    Методы
     */

    fun clearAllMarkType() {
        isMan.value = false
        isWoman.value = false
        isChildren.value = false
        isUnisex.value = false
    }

    fun onClickSearch() {
        launchUITryCatch {
            manager.loadSearchTaskList(TaskSearchParams(
                    material = number.value.orEmpty(),
                    markType = getMarkType()
            ))

            navigator.goBack()
        }
    }

    private fun getMarkType(): String {
        return when {
            isMan.value == true -> ShoesMarkType.MAN.code
            isWoman.value == true -> ShoesMarkType.WOMAN.code
            isChildren.value == true -> ShoesMarkType.CHILDREN.code
            isUnisex.value == true -> ShoesMarkType.UNISEX.code
            else -> ShoesMarkType.UNKNOWN.code
        }
    }

    fun onScanResult(data: String) {
        actionByNumber(
                number = data,
                funcForNotValidFormat = navigator::showIncorrectEanFormat
        )
    }

}