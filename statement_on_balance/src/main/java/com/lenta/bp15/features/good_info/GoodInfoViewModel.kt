package com.lenta.bp15.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lenta.bp15.features.good_list.GoodListFragment
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.mapSkipNulls
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var resource: IResourceManager

    @Inject
    lateinit var manager: ITaskManager


    /**
    Переменные
     */

    private val task by lazy {
        manager.currentTask
    }

    private val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { it.getShortMaterialWithName() }
    }

    private val scannedMarks = MutableLiveData(mutableListOf<String>())

    val quantity = scannedMarks.map { it.size }

    val quantityWithUnits = quantity.map { quantity ->
        "$quantity ${Uom.ST.name}"
    }

    val goodInfo by lazy {
        good.mapSkipNulls { good ->
            GoodInfoUi(
                    markType = good.markType.description,
                    matrix = good.matrix,
                    section = good.section
            )
        }
    }

    private val processedMarks by lazy {
        good.map { it.getProcessedMarksCount() }
    }

    private val totalMarks by lazy {
        good.map { it.planQuantity }
    }

    val markScanProgress by lazy {
        quantity.map { currentScannedQuantity ->
            val processed = processedMarks.value ?: 0 + currentScannedQuantity
            val total = totalMarks.value ?: 0
            resource.processingProgress("$processed", "$total")
        }
    }

    val allMarkProcessed by lazy {
        quantity.map { currentScannedQuantity ->
            val processed = processedMarks.value ?: 0 + currentScannedQuantity
            val total = totalMarks.value ?: 0
            processed == total
        }
    }

    val applyEnabled = quantity.map { it > 0 }

    val rollbackEnabled = quantity.map { it > 0 }

    /**
    Методы
     */

    fun onClickRollback() {
        scannedMarks.value?.let { marks ->
            marks.removeAt(marks.lastIndex)
            scannedMarks.value = marks
        }
    }

    fun onClickApply() {

    }

    fun onScanResult(data: String) {

    }

    fun onBackPressed() {
        quantity.value?.let { quantity ->
            if (quantity > 0) {
                navigator.showUnsavedDataWillBeRemoved {
                    navigator.goBackTo(GoodListFragment::class.simpleName)
                }
            } else {
                navigator.goBack()
            }
        }
    }

}