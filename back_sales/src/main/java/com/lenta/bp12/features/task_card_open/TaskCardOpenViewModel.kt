package com.lenta.bp12.features.task_card_open

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.ITaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskCardOpenViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var manager: ITaskManager


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val task by lazy {
        manager.currentTask
    }

    val nextEnabled = MutableLiveData(false)

    val selectedPage = MutableLiveData(0)

    val ui by lazy {
        task.map {
            it?.let { task ->
                TaskCardOpenUi(
                        type = task.type?.description ?: "",
                        name = task.name,
                        provider = task.getProviderCodeWithName(),
                        storage = task.storage,
                        reason = task.reason.description,
                        description = task.type?.description ?: "",
                        comment = task.comment,
                        isStrict = task.isStrict,
                        isAlcohol = task.control == ControlType.ALCOHOL,
                        isCommon = task.control == ControlType.COMMON
                )
            }
        }
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            //taskTypes.value = database.getTaskTypeList()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickNext() {
        navigator.openGoodListScreen()
    }

}

data class TaskCardOpenUi(
        val type: String,
        val name: String,
        val provider: String,
        val storage: String,
        val reason: String,
        val description: String,
        val comment: String,
        val isStrict: Boolean,
        val isAlcohol: Boolean,
        val isCommon: Boolean
)