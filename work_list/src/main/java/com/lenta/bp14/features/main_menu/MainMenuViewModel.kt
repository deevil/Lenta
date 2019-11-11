package com.lenta.bp14.features.main_menu

import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_list.CheckListTaskDescription
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var checkListTaskManager: CheckListTaskManager
    @Inject
    lateinit var generalRepo: IGeneralRepo
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager


    private val authorizationSkipped by lazy {
        sessionInfo.isAuthSkipped.map { it == true }
    }

    val authorizationButtonVisibility by lazy { authorizationSkipped.map { it } }


    val createTaskButtonVisibility by lazy { authorizationSkipped.map { it == false } }
    val workWithTaskButtonVisibility by lazy { authorizationSkipped.map { it == false } }
    val checkListButtonVisibility by lazy { authorizationSkipped.map { it == true } }


    fun onClickPrint() {
        navigator.openPrintSettingsScreen()
    }

    fun onClickCreateTask() {
        navigator.openJobCardScreen()
    }

    fun onClickWorkWithTask() {
        navigator.openTaskListScreen()
    }

    fun onClickCheckList() {
        generalRepo.getTasksTypeInfo(AppTaskTypes.CheckList.taskType)?.let { taskTypeInfo ->
            checkListTaskManager.newTask(
                    CheckListTaskDescription(
                            tkNumber = sessionInfo.market!!,
                            taskNumber = "",
                            taskName = generalTaskManager.generateNewNameForTask(taskTypeInfo),
                            comment = "",
                            description = taskTypeInfo.annotation,
                            isStrictList = false
                    )
            )

            if (sessionInfo.isAuthSkipped.value == false) {
                navigator.openJobCardScreen()
            }

            navigator.openGoodsListClScreen()
        }
    }

    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    fun onClickAuthorization() {
        navigator.closeAllScreen()
        navigator.openLoginScreen()
    }

}