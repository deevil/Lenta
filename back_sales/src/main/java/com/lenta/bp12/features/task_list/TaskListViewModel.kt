package com.lenta.bp12.features.task_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.TaskStatus
import com.lenta.shared.utilities.databinding.PageSelectionListener

class TaskListViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    // TODO: Implement the ViewModel
}

data class ItemTaskUi(
        val position: String,
        val name: String,
        val supplier: String,
        val taskStatus: TaskStatus,
        val blockType: BlockType,
        val quantity: String
)