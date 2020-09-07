package com.lenta.bp9.features.label_printing.print_labels_count_copies

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.features.label_printing.LabelPrintingItem
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class PrintLabelsCountCopiesViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var context: Context

    private val labels: MutableLiveData<List<LabelPrintingItem>> = MutableLiveData()

    val  isVisibilityProductionDate: MutableLiveData<Boolean> = labels.map {
        it?.size == 1
    }

    val tvProductionDate: MutableLiveData<String> = isVisibilityProductionDate.map {
        if (it == true) {
            labels.value?.getOrNull(FIRST_LABEL)?.productionDate.orEmpty()
        } else {
            ""
        }
    }

    val countCopies: MutableLiveData<String> = MutableLiveData(DEFAULT_COUNT_COPiES)
    val tvPrintingByGoods: MutableLiveData<String> = MutableLiveData()

    val enabledConfirmBtn = countCopies.map {
        val countCopiesValue = it?.toIntOrNull() ?: 0
        countCopiesValue > 0
    }

    init {
        launchUITryCatch {
            tvPrintingByGoods.value =
                    if (isVisibilityProductionDate.value == true) {
                        "${context.getString(R.string.good)} ${labels.value?.getOrNull(FIRST_LABEL)?.productName.orEmpty()}"
                    } else {
                        context.getString(R.string.printing_by_goods)
                    }
        }
    }

    fun initLabels(_labels: List<LabelPrintingItem>) {
        labels.value = _labels
    }

    fun getTitle(): String {
        return taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.caption
                .orEmpty()
    }

    fun onClickConfirm() {
        val printedLabels: ArrayList<String> = ArrayList()
        labels.value?.mapTo(printedLabels) {it.copy().batchNumber}

        screenNavigator.goBackWithArgs(Bundle().apply {
            putStringArrayList("printedLabels", printedLabels)
        })
    }

    companion object {
        private const val DEFAULT_COUNT_COPiES = "1"
        private const val FIRST_LABEL = 0
    }
}
