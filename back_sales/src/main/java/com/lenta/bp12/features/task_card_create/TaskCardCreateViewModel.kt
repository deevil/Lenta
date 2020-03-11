package com.lenta.bp12.features.task_card_create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.dao_ext.TaskType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TaskCardCreateViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var database: IDatabaseRepository


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val nextEnabled = MutableLiveData(false)

    val selectedPage = MutableLiveData(0)

    val cardEditable = MutableLiveData(true)

    val taskName = MutableLiveData("Возврат от ${SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(Date())}")

    val ui by lazy {
        TaskCardUi(
                provider = "568932 ООО Микоян",
                description = "Возврат прямому поставщику",
                comment = "Комплектование необходимо выполнить до 16:00!!!",
                isStrict = true,
                isAlcohol = false,
                isCommon = true
        )
    }

    val taskTypePosition = MutableLiveData(0)

    val reasonForReturnPosition = MutableLiveData(0)

    val storagePosition = MutableLiveData(0)

    val onSelectTaskType = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            taskTypePosition.value = position
        }
    }

    val onSelectReasonForReturn = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            taskTypePosition.value = position
        }
    }

    val onSelectStorage = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            taskTypePosition.value = position
        }
    }

    private val taskTypes = MutableLiveData<List<TaskType>>(emptyList())

    val taskTypeList: MutableLiveData<List<String>> by lazy {
        taskTypes.map { list ->
            list?.map { it.description }
        }
    }

    val reasonForReturnList: MutableLiveData<List<String>> by lazy {
        MutableLiveData(List(3) {
            "Reason for return ${it + 1}"
        })
    }

    val storageList: MutableLiveData<List<String>> by lazy {
        MutableLiveData(List(3) {
            "Storage ${it + 1}"
        })
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            taskTypes.value = database.getTaskTypeList()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickNext() {

    }

}

data class TaskCardUi(
        val provider: String,
        val description: String,
        val comment: String,
        val isStrict: Boolean,
        val isAlcohol: Boolean,
        val isCommon: Boolean
)