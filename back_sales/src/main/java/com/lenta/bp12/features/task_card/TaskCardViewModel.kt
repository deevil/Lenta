package com.lenta.bp12.features.task_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class TaskCardViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val nextEnabled = MutableLiveData(false)

    val selectedPage = MutableLiveData(0)

    val cardEditable = MutableLiveData(false)

    val taskName = MutableLiveData("")

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

    val taskTypeList: MutableLiveData<List<String>> by lazy {
        MutableLiveData(List(3) {
            "Task type ${it + 1}"
        })
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