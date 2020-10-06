package com.lenta.bp15.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.model.pojo.Mark
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
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

    private val currentQuantity = scannedMarks.map { it.size }

    val currentQuantityWithUnits = currentQuantity.map { quantity ->
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

    private var allTaskMarks = emptyMap<String, Mark>()

    private val processedQuantity by lazy {
        good.map { it.getProcessedMarksCount() }
    }

    private val totalMarks by lazy {
        good.map { it.planQuantity }
    }

    val markScanProgress by lazy {
        processedQuantity.combineLatest(currentQuantity).combineLatest(totalMarks).map {
            val processed = it.first.first
            val current = it.first.second
            val total = it.second

            resource.processingProgress("${processed + current}", "$total")
        }
    }

    val allMarkProcessed by lazy {
        processedQuantity.combineLatest(currentQuantity).combineLatest(totalMarks).map {
            val processed = it.first.first
            val current = it.first.second
            val total = it.second

            (processed + current) == total
        }
    }

    val applyEnabled = currentQuantity.map { it > 0 }

    val rollbackEnabled = currentQuantity.map { it > 0 }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            navigator.showProgressLoadingData()

            allTaskMarks = task.value?.getAllMarks() ?: emptyMap()

            navigator.hideProgress()
        }
    }

    /**
    Методы
     */

    fun onScanResult(data: String) {
        actionByNumber(
                number = data,
                funcForShoes = ::checkScannedMark,
                funcForNotValidFormat = navigator::showIncorrectEanFormat
        )
    }

    private fun checkScannedMark(number: String) {
        good.value?.let { good ->
            allTaskMarks.get(number)?.let { mark ->
                val isExistInScanList = scannedMarks.value?.contains(number) ?: false
                when {
                    mark.isScan || isExistInScanList -> navigator.showMarkAlreadyProcessed()
                    mark.material != good.material -> navigator.showScannedMarkBelongsToAnotherGood(good.material)
                    else -> addMarkToList(number)
                }
            } ?: navigator.showScannedMarkIsNotOnTask()
        }
    }

    private fun addMarkToList(number: String) {
        scannedMarks.value?.let { marks ->
            marks.add(number)
            scannedMarks.value = marks
        }
    }

    fun onBackPressed() {
        currentQuantity.value?.let { quantity ->
            if (quantity > 0) {
                navigator.showUnsavedDataWillBeRemoved {
                    manager.removeCurrentTaskBackup()
                    navigator.goBack()
                    navigator.goBack()
                }
            } else {
                navigator.goBack()
            }
        }
    }

    fun onClickRollback() {
        scannedMarks.value?.let { marks ->
            marks.removeAt(marks.lastIndex)
            scannedMarks.value = marks
        }
    }

    fun onClickApply() {
        good.value?.let { good ->
            navigator.showProgressLoadingData()

            scannedMarks.value?.let { marks ->
                good.changeScanStatusFor(marks)
            }

            manager.backupCurrentTask()

            navigator.hideProgress()

            navigator.goBack()
        }
    }

}