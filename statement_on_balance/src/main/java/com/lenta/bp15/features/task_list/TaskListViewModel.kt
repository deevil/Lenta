package com.lenta.bp15.features.task_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.BlockType
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
    }

    val numberField = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(false)

    val taskList = MutableLiveData(
            List((3..7).random()) {
                val position = (it + 1).toString()
                ItemTaskUi(
                        position = position,
                        name = "Test name $position",
                        description = "Test description $position",
                        isFinished = false,
                        blockType = BlockType.UNLOCK,
                        quantity = (1..25).random().toString()
                )
            }
    )

    val searchList = MutableLiveData(
            List((3..7).random()) {
                val position = (it + 1).toString()
                ItemTaskUi(
                        position = position,
                        name = "Test name $position",
                        description = "Test description $position",
                        isFinished = false,
                        blockType = BlockType.UNLOCK,
                        quantity = (1..25).random().toString()
                )
            }
    )

    override fun onPageSelected(position: Int) {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

    fun onClickItemTaskPosition(position: Int) {

    }

    fun onClickItemSearchPosition(position: Int) {

    }

    fun onScanResult(data: String) {

    }

    fun onClickUpdate() {

    }

}