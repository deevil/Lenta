package com.lenta.inventory.features.main_menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.TaskDescription
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val fio = MutableLiveData("")

    init {
        viewModelScope.launch {
            fio.value = sessionInfo.personnelFullName
        }
    }

    fun onClickWorkWithTask() {
        //screenNavigator.openLoadingTasksScreen()

        val taskDescription = TaskDescription(
                taskNumber = "4485",
                taskName = "12 неделя",
                taskType = "ВИ",
                stock = "0001",
                isRecount = true,
                isStrict = true,
                blockType = "",
                lockUser = "",
                lockIP = "",
                productsInTask = 6,
                isStarted = true,
                dateFrom = "2019-03-18",
                dateTo = "2019-07-24",
                taskDeadLine = "2019-07-25",
                recountType = RecountType.None,
                gis = GisControl.GeneralProduct,
                linkOldStamp = false
        )


        val productInfo6 = TaskProductInfo("000000000000378167", "Набор Коньяк СТАРЫЙ КЕНИГСБЕРГ Лего (Стар. Кениг 4г, 5 лет и КВ) п/у 3*0.25L алк.40% (Россия) 0.75L", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                true, "01", MatrixType.Active, "materialType","00", 1.0, false, isExcOld = false)
        val productInfo7 = TaskProductInfo("000077", "Виски", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","2", 2.0, true, isExcOld = false)
        val productInfo8 = TaskProductInfo("000077", "Виски", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","3", 0.0, false, isExcOld = false)

        val exciseStamp1 = TaskExciseStamp("000000000000377983", "151869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060", "00", "boxNumber1", "000000000000378167", "", "", false)
        val exciseStamp2 = TaskExciseStamp("000000000000377982", "251869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060", "00", "boxNumber1", "000000000000378167", "", "", false)
        val exciseStamp3 = TaskExciseStamp("000000000000377980", "351869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060", "00", "boxNumber1", "000000000000378167", "", "", false)

        val exciseStamp4 = TaskExciseStamp("000000000000349347", "451869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060", "00", "boxNumber1", "000000000000430774", "", "", false)
        val exciseStamp5 = TaskExciseStamp("000000000000349347", "551869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5061", "00", "boxNumber2", "000000000000430774", "", "", false)
        val exciseStamp6 = TaskExciseStamp("00000000000034934", "651869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5061", "00", "boxNumber2", "000000000000430774", "", "", false)

        val exciseStamp7 = TaskExciseStamp("000077", "22N0000154KNI691XDC380V71231001511013ZZ012345678901234567890123456ZZ", "2", "boxNumber3", "", "", "", false)
        val exciseStamp8 = TaskExciseStamp("000077", "22N0000154KNI691XDC380V71231001513730ZZ012345678901234567890123456ZZ", "2", "boxNumber3", "", "", "", false)



        if (processServiceManager.getInventoryTask() == null) {
            processServiceManager.newInventoryTask(taskDescription = taskDescription)
        }
        //processServiceManager.getInventoryTask()!!.taskRepository.getProducts().deleteProduct(productInfo6)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo6)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp1)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp2)
        //processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp3)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp4)
        //processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp5)
        //processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp6)

        /**processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo2)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo3)

        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo4)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo5)

        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().deleteProduct(productInfo6)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo6)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo7)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo8)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp1)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp2)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp3)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp4)*/

        screenNavigator.openSetsInfoScreen(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo6)!!)
    }

    fun onClickUser() {
        screenNavigator.openSelectionPersonnelNumberScreen()

    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}