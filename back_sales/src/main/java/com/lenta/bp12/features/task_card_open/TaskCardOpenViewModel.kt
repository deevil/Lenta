package com.lenta.bp12.features.task_card_open

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
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
    lateinit var openTaskManager: IOpenTaskManager


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val nextEnabled = MutableLiveData(false)

    val selectedPage = MutableLiveData(0)

    val ui by lazy {
        TaskCardOpenUi(
                type = "Возврат брака прямому поставщику",
                name = "Возврат от 05.08.2020 15:16",
                supplier = "568932 ООО Микоян",
                storage = "0010",
                reason = "Нарушение товарного вида",
                description = "Возврат прямому поставщику",
                comment = "Комплектование необходимо выполнить до 16:00!!!",
                isStrict = false,
                isAlcohol = false,
                isCommon = true
        )
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
        val supplier: String,
        val storage: String,
        val reason: String,
        val description: String,
        val comment: String,
        val isStrict: Boolean,
        val isAlcohol: Boolean,
        val isCommon: Boolean
)