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
                gis = GisControl.GeneralProduct
        )

        val productInfo6 = TaskProductInfo("000077", "Виски", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","00", 2.0, true)
        /**val productInfo7 = TaskProductInfo("000077", "Виски", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","2", 2.0, true)
        val productInfo8 = TaskProductInfo("000077", "Виски", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","3", null, false)

        val exciseStamp1 = TaskExciseStamp("000077", "951869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060", "1", "boxNumber1", "", "", null, false)
        val exciseStamp2 = TaskExciseStamp("000077", "951869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5061", "1", "boxNumber2", "", "", null, false)
        val exciseStamp3 = TaskExciseStamp("000077", "22N0000154KNI691XDC380V71231001511013ZZ012345678901234567890123456ZZ", "2", "boxNumber3", "", "", null, false)
        val exciseStamp4 = TaskExciseStamp("000077", "22N0000154KNI691XDC380V71231001513730ZZ012345678901234567890123456ZZ", "2", "boxNumber3", "", "", null, false)
*/


        if (processServiceManager.getInventoryTask() == null) {
            processServiceManager.newInventoryTask(taskDescription = taskDescription)
        }
        //processServiceManager.getInventoryTask()!!.taskRepository.getProducts().deleteProduct(productInfo6)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo6)
        /**processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo7)
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(productInfo8)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp1)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp2)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp3)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamp(exciseStamp4)*/

        screenNavigator.openExciseAlcoInfoScreen(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo6)!!)
    }

    fun onClickUser() {
        screenNavigator.openSelectionPersonnelNumberScreen()

    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}