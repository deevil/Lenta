package com.lenta.bp9.features.main_menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.ReceivingProductInfo
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    //todo delete
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val fio = MutableLiveData("")

    init {
        viewModelScope.launch {
            fio.value = sessionInfo.personnelFullName
        }
    }

    fun onClickShipmentTask() {

    }

    fun onClickReceiptTask() {
        //todo delete
        val taskInfo = TaskInfo(
                position = "0",
                taskNumber = "",
                bottomText = "",
                caption = "ППП-27855//ПП-108825 ООО \"СН...\"",
                documentNumber = "",
                isCracked = false,
                isDelayed = false,
                isPaused = false,
                isStarted = false,
                lockStatus = TaskLockStatus.None,
                positionsCount = 0,
                status = TaskStatus.Completed,
                taskType = TaskType.None,
                topText = "",
                transportationOTM = ""
        )
        val taskDescription = TaskDescription(
                currentStatus = TaskStatus.Completed,
                actualArrivalDate = "",
                actualArrivalTime = "",
                currentStatusDate = "",
                currentStatusText = "",
                currentStatusTime = "",
                deliveryNumber = "",
                isAlco = false,
                isNotEDI = false,
                isOverdue = false,
                isOwnTransport = false,
                isPromo = false,
                isRawMaterials = false,
                isRecount = false,
                isSpecialControlGoods = false,
                isSupplierReturnAvailability = false,
                isUFF = false,
                nextStatusDate = "",
                nextStatusText = "",
                nextStatusTime = "",
                orderNumber = "",
                plannedDeliveryDate = "",
                plannedDeliveryTime = "",
                quantityPositions = 0,
                ttnNumber = ""
                )
        taskManager.newReceivingTask(taskInfo,taskDescription)
        taskManager.getReceivingTask().let {
            it!!.taskRepository.getProducts().addProduct(ReceivingProductInfo(
                    materialNumber = "000021",
                    description = "Р/к горбуша (Россия) 230/250г",
                    uom = Uom("ST", "шт"),
                    type = ProductType.General,
                    isSet = false,
                    sectionId = "01",
                    matrixType = MatrixType.Active,
                    materialType = "",
                    origQuantity = "",
                    orderQuantity = "",
                    quantityCapitalized = "",
                    overdToleranceLimit = "",
                    underdToleranceLimit = "",
                    upLimitCondAmount = "",
                    quantityInvest = "",
                    roundingSurplus = "",
                    roundingShortages = "",
                    isNoEAN = false,
                    isWithoutRecount = false,
                    isUFF = false,
                    isNotEdit = false,
                    totalExpirationDate = "",
                    remainingShelfLife = "",
                    isRus = false,
                    isBoxFl = false,
                    isMarkFl = false,
                    isVet = false,
                    numberBoxesControl = "",
                    numberStampsControl = ""
            ))
        }
        taskManager.getReceivingTask().let {
            it!!.taskRepository.getProducts().addProduct(ReceivingProductInfo(
                    materialNumber = "000017",
                    description = "Масло",
                    uom = Uom("ST", "шт"),
                    type = ProductType.General,
                    isSet = false,
                    sectionId = "01",
                    matrixType = MatrixType.Active,
                    materialType = "",
                    origQuantity = "",
                    orderQuantity = "",
                    quantityCapitalized = "",
                    overdToleranceLimit = "",
                    underdToleranceLimit = "",
                    upLimitCondAmount = "",
                    quantityInvest = "",
                    roundingSurplus = "",
                    roundingShortages = "",
                    isNoEAN = true,
                    isWithoutRecount = false,
                    isUFF = false,
                    isNotEdit = false,
                    totalExpirationDate = "",
                    remainingShelfLife = "",
                    isRus = false,
                    isBoxFl = false,
                    isMarkFl = false,
                    isVet = false,
                    numberBoxesControl = "",
                    numberStampsControl = ""
            ))
        }
        screenNavigator.openGoodsListScreen()

        //screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.Receiving)
    }

    fun onClickRecountTask() {

    }

    fun onClickUser() {
        screenNavigator.openSelectionPersonnelNumberScreen()

    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}
