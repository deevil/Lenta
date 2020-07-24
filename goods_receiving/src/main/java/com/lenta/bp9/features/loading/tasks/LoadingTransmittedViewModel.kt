package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.mercury_list_irrelevant.ZMP_UTZ_GRZ_13_V001
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.TransmittedNetRequest
import com.lenta.bp9.requests.network.TransmittedParams
import com.lenta.bp9.requests.network.TransmittedRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingTransmittedViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var transmittedNetRequest: TransmittedNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var hyperHive: HyperHive

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
            progress.value = true
            taskManager.getReceivingTask()?.let { task ->
                val params = TransmittedParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personnelNumber = sessionInfo.personnelNumber.orEmpty(),
                        printerName = sessionInfo.printer.orEmpty(),
                        unbindVSD = ""
                )
                transmittedNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    private fun handleSuccess(result: TransmittedRestInfo) {
        viewModelScope.launch {
            val mercuryNotActual = result.taskMercuryNotActualRestData.map { TaskMercuryNotActual.from(hyperHive,it)}
            val sectionInfo = result.sectionsInfo.map { TaskSectionInfo.from(it) }
            val sectionProducts = result.sectionProducts.map { TaskSectionProducts.from(hyperHive, it) }
            val notifications = result.notifications.map { TaskNotification.from(it) }
            taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.updateMercuryNotActual(mercuryNotActual)
            taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
            taskManager.getReceivingTask()?.taskRepository?.getDocumentsPrinting()?.updateDocumentsPrinting(result.listDocumentsPrinting.map { TaskDocumentsPrinting.from(it) })
            taskManager.getReceivingTask()?.taskRepository?.getSections()?.updateSections(sectionInfo, sectionProducts)

            if (mercuryNotActual.isNotEmpty()) {
                screenNavigator.openMainMenuScreen()
                screenNavigator.openTaskListScreen()
                screenNavigator.openAlertElectronicVadLostRelevance(
                        browsingCallbackFunc = {
                            screenNavigator.openMercuryListIrrelevantScreen(ZMP_UTZ_GRZ_13_V001)
                        },
                        countVad = mercuryNotActual.size.toString(),
                        countGoods = taskManager.getReceivingTask()?.taskDescription?.quantityPositions.toString()
                )
                return@launch
            }

            when (taskManager.getReceivingTask()?.taskHeader?.taskType) {
                TaskType.DirectSupplier -> {
                    taskManager.getReceivingTask()?.taskDescription?.currentStatus.let {
                        if (it == TaskStatus.ShipmentSentToGis) {
                            screenNavigator.openMainMenuScreen()
                            screenNavigator.openTaskListScreen()
                            screenNavigator.openAlertDeliveryDdataWasSentToGISScreen()
                        } else {
                            screenNavigator.openSupplyResultsScreen(
                                    pageNumber = "78",
                                    numberSupply = taskManager.getReceivingTask()?.taskDescription?.deliveryNumber ?: "",
                                    isAutomaticWriteOff = false
                            )
                        }
                    }
                }
                TaskType.RecalculationCargoUnit -> {
                    specialControlGoods()
                }
                TaskType.ShipmentPP -> {
                    if (result.supplierShipmentPP.isNotEmpty()) {
                        /**"По текущему поставщику {0} есть доступные задания на возврат. Перейти к заданиям на отгрузку?"
                        и переходим на список заданий БКС на вкладку поиск и вызываем ZMP_UTZ_GRZ_02 с структурой поиска где LIFNR = EV_BKS_LIFNR*/
                        screenNavigator.openMainMenuScreen()
                        screenNavigator.openTaskListScreen()
                        screenNavigator.openCurrentProviderHasReturnJobsAvailableDialog(
                                numberCurrentProvider = "",
                                nextCallbackFunc = {
                                    screenNavigator.goBack()
                                    //todo "переходим на список заданий БКС на вкладку поиск и вызываем ZMP_UTZ_GRZ_02 с структурой поиска где LIFNR = EV_BKS_LIFNR" - пока не реализовано, т.к. нет задания для теста, просто переходим назад
                                }
                        )
                    } else {
                        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
                    }
                }
                else -> {
                    screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
                }
            }
        }
    }

    private fun specialControlGoods() {
        if (taskManager.getReceivingTask()?.taskDescription?.isSpecialControlGoods == true) {
            /**В задании присутствуют товары особого контроля. Необходимо указать представителей от секций получивших товар" и переход на обработку секций*/
            screenNavigator.openTransferGoodsSectionScreen()
            screenNavigator.openAlertHaveIsSpecialGoodsScreen()
        } else {
            /**В задании отсутствуют товары особого контроля. Товары будут автоматически переданы в секцию." и переход на список заданий*/
            screenNavigator.openMainMenuScreen()
            screenNavigator.openTaskListScreen()
            screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
            screenNavigator.openAlertNoIsSpecialGoodsScreen()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    override fun clean() {
        progress.postValue(false)
    }
}