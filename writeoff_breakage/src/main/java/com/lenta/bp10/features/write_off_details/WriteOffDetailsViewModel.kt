package com.lenta.bp10.features.write_off_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class WriteOffDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    val selectionsHelper = SelectionItemsHelper()

    val productInfoLiveData = MutableLiveData<ProductInfo>()

    val deleteButtonEnabled = selectionsHelper.selectedPositions.map { !it.isNullOrEmpty() }

    val items: MutableLiveData<List<ReasonItem>> = productInfoLiveData.map { productInfo ->
        getReasons(productInfo)
    }


    private fun getReasons(productInfo: ProductInfo?): List<ReasonItem> {
        val res = mutableListOf<ReasonItem>()
        if (productInfo != null) {
            processServiceManager.getWriteOffTask()?.apply {
                this.taskRepository
                        .getWriteOffReasons()
                        .findWriteOffReasonsOfProduct(productInfo)
                        .reversed()
                        .mapIndexed { index, taskWriteOffReason ->
                            res.add(
                                    ReasonItem(
                                            number = index + 1,
                                            name = taskWriteOffReason.writeOffReason.name,
                                            quantity = "${taskWriteOffReason.count} ${productInfo.uom.name}",
                                            even = index % 2 == 0,
                                            taskWriteOffReason = taskWriteOffReason
                                    )
                            )
                        }
            }
        }

        return res
    }

    fun onClickDelete() {
        processServiceManager.getWriteOffTask()?.let { writeOffTask ->
            selectionsHelper.selectedPositions.value?.map { position ->
                items.value!![position].let { reasonItem ->
                    writeOffTask.deleteTaskWriteOffReason(reasonItem.taskWriteOffReason)
                }
            }
        }

        productInfoLiveData.value = productInfoLiveData.value

        selectionsHelper.clearPositions()

    }

}

data class ReasonItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val taskWriteOffReason: TaskWriteOffReason
) : Evenable {
    override fun isEven() = even

}
