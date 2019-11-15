package com.lenta.bp16.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.model.pojo.Task
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val selectedPage = MutableLiveData(0)

    val tasks = MutableLiveData<List<Task>>(emptyList())

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(true)

    private val toUiFunc = { products: List<Task>? ->
        products?.mapIndexed { index, task ->
            ItemTaskListUi(
                    position = (index + 1).toString(),
                    puNumber = task.puNumber,
                    taskType = TaskType.DEFROZE,
                    sku = "0"
            )
        }
    }

    val processing by lazy {
        tasks.map { it?.filter { task -> !task.isProcessed } }.map(toUiFunc)
    }

    val processed by lazy {
        tasks.map { it?.filter { task -> task.isProcessed } }.map(toUiFunc)
    }

    // -----------------------------

    init {
        viewModelScope.launch {

        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        //selectedPage.value = position
    }

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickRefresh() {
        // Обновить список заданий

    }

    fun onClickItemPosition(position: Int) {
        // Открытие нужного списка товаров

    }

}

data class ItemTaskListUi(
        val position: String,
        val puNumber: String,
        val taskType: TaskType,
        val sku: String
)