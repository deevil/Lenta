package com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_product_failure

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class ExciseAlcoBoxProductFailureViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processExciseAlcoBoxAccService: ProcessExciseAlcoBoxAccService
    @Inject
    lateinit var dataBase: IDataBaseRepo

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    private val paramGrzCrGrundcatCode: MutableLiveData<String> = MutableLiveData("")
    private val paramGrzCrGrundcatName: MutableLiveData<String> = MutableLiveData("")

    val enabledPartialFailureBtn: MutableLiveData<Boolean> by lazy {
        /** https://trello.com/c/WeGFSdAW
         * Частичный отказ. Кнопка становится доступной для нажатия, если по товару имеется хотя бы один обработанный короб. Логика обработанных коробов описана в тикете
         * проверка проводится как и в ф-ции processExciseAlcoBoxAccService.boxControl(boxInfo) только для всех коробов, и если хоть один обработанный, то делаем кнопку активной
         */
        MutableLiveData((taskManager.getReceivingTask()?.taskRepository?.getBoxesRepository()?.findBoxesOfProduct(productInfo.value!!)?.filter { taskBoxInfo ->
            val countScannedBoxex = taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo.value!!)?.filter {
                it.isScan && it.boxNumber == taskBoxInfo.boxNumber
            }?.size ?: 0
            val countScannedExciseStamps = taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.findExciseStampsDiscrepanciesOfProduct(productInfo.value!!)?.filter {
                it.isScan && it.boxNumber == taskBoxInfo.boxNumber
            }?.size ?: 0
            (countScannedBoxex >= 1 && countScannedExciseStamps >= 1) || (countScannedExciseStamps >= 2) || (countScannedExciseStamps >= productInfo.value!!.numberStampsControl.toInt())
        }?.size ?: 0) > 0)
    }

    init {
        launchUITryCatch {
            paramGrzCrGrundcatCode.value = dataBase.getParamGrzCrGrundcat() ?: ""
            paramGrzCrGrundcatName.value = dataBase.getGrzCrGrundcatName(paramGrzCrGrundcatCode.value!!) ?: ""
        }
    }

    fun onClickCompleteRejection() {
        if (processExciseAlcoBoxAccService.newProcessExciseAlcoBoxService(productInfo.value!!) == null){
            screenNavigator.openAlertWrongProductType()
        } else {
            screenNavigator.openCompleteRejectionOfGoodsDialog(
                    applyCallbackFunc = {
                        screenNavigator.goBack()
                        processExciseAlcoBoxAccService.denialOfFullProductAcceptance(paramGrzCrGrundcatCode.value ?: "")
                    },
                    title = "${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}",
                    countBoxes = productInfo.value!!.origQuantity.toDouble().toStringFormatted(),
                    paramGrzCrGrundcatName = paramGrzCrGrundcatName.value ?: ""
            )
        }
    }

    fun onClickPartialFailure() {
        if (processExciseAlcoBoxAccService.newProcessExciseAlcoBoxService(productInfo.value!!) == null){
            screenNavigator.openAlertWrongProductType()
        } else {
            screenNavigator.openPartialRefusalOnGoodsDialog(
                    applyCallbackFunc = {
                        screenNavigator.goBack()
                        processExciseAlcoBoxAccService.refusalToAcceptPartlyByProduct(paramGrzCrGrundcatCode.value ?: "")
                    },
                    title = "${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}",
                    countScanBoxes = processExciseAlcoBoxAccService.getCountBoxesProcessedOfProduct().toString(),
                    unconfirmedQuantity = (productInfo.value!!.origQuantity.toDouble() - processExciseAlcoBoxAccService.getCountBoxesProcessedOfProduct()).toStringFormatted(),
                    paramGrzCrGrundcatName = paramGrzCrGrundcatName.value ?: ""
            )
        }
    }

}
