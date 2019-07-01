package com.lenta.inventory.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }
    val tasks = MutableLiveData<List<TaskItem>>()
    val tasksCount = tasks.map { tasks.value?.size ?: 0 }

    val filerTitles = MutableLiveData<List<String>>()
    val filerPosition = MutableLiveData<Int>()

    init {
        generateTestData()
    }

    private fun generateTestData() {
        viewModelScope.launch {
            tasks.value = listOf(
                    TaskItem(
                            number = "3",
                            title = "ВИ-304-Акционка",
                            stock = "Склад 0001",
                            typeConversion = TypeConversion.Secondary,
                            statusTask = StatusTask.BlockedMe,
                            count = "55"
                    ),
                    TaskItem(
                            number = "2",
                            title = "КИ-303-Секция",
                            stock = "Склад 0001",
                            typeConversion = TypeConversion.Secondary,
                            statusTask = StatusTask.Free,
                            count = "29"
                    ),
                    TaskItem(
                            number = "1",
                            title = "ЦИ-311-Срочно",
                            stock = "Склад 0001",
                            typeConversion = TypeConversion.Secondary,
                            statusTask = StatusTask.Free,
                            count = "432"
                    )
            )

            filerTitles.value = listOf("MAKAROV", "DENISENKO", "OVCHARENKO", "BORISENKO")

        }

    }

    override fun onClickPosition(position: Int) {
        filerPosition.value = position
    }

}

data class TaskItem(
        val number: String,
        val title: String,
        val stock: String,
        val typeConversion: TypeConversion,
        val statusTask: StatusTask,
        val count: String

)

enum class TypeConversion {
    Primary,
    Secondary,
    Parallels

}

enum class StatusTask {
    Free,
    BlockedMe,
    BlockedNotMe,
    Processed

}
