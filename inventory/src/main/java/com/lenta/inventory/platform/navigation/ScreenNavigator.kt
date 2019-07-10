package com.lenta.inventory.platform.navigation

import android.content.Context
import com.lenta.inventory.R
import com.lenta.inventory.features.main_menu.MainMenuFragment
import com.lenta.inventory.features.auth.AuthFragment
import com.lenta.inventory.features.discrepancies_found.DiscrepanciesFoundFragment
import com.lenta.inventory.features.goods_details_storage.GoodsDetailsStorageFragment
import com.lenta.inventory.features.goods_information.excise_alco.ExciseAlcoInfoFragment
import com.lenta.inventory.features.goods_information.general.GoodsInfoFragment
import com.lenta.inventory.features.goods_information.sets.SetsInfoFragment
import com.lenta.inventory.features.goods_information.sets.components.SetComponentsFragment
import com.lenta.inventory.features.goods_list.GoodsListFragment
import com.lenta.inventory.features.job_card.JobCardFragment
import com.lenta.inventory.features.loading.fast.FastDataLoadingFragment
import com.lenta.inventory.features.loading.store_place_lock.LoadingStorePlaceLockFragment
import com.lenta.inventory.features.loading.tasks.LoadingTaskContentFragment
import com.lenta.inventory.features.loading.tasks.LoadingTasksFragment
import com.lenta.inventory.features.select_market.SelectMarketFragment
import com.lenta.inventory.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.inventory.features.storages_list.StoragesListFragment
import com.lenta.inventory.features.task_list.TaskItemVm
import com.lenta.inventory.features.task_list.TaskListFragment
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.TaskStorePlaceInfo
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo

class ScreenNavigator(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.let {
                it.popAll()
                it.replace(AuthFragment())
            }
        }

    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectMarketFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openSelectionPersonnelNumberScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectPersonnelNumberFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(MainMenuFragment())
        }
    }

    override fun openGoodsInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment.create(productInfo))
        }
    }

    override fun openExciseAlcoInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoInfoFragment())
        }
    }

    override fun openGoodsDetailsStorageScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsDetailsStorageFragment.create(productInfo))
        }
    }

    override fun openGoodsListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment())
        }
    }

    override fun openSetsInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SetsInfoFragment())
        }
    }

    override fun openSetComponentsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SetComponentsFragment())
        }
    }

    override fun openStoragesList() {
        runOrPostpone {
            getFragmentStack()?.push(StoragesListFragment())
        }
    }

    override fun openTasksList() {
        runOrPostpone {
            getFragmentStack()?.push(TaskListFragment())
        }
    }

    override fun openDiscrepanciesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DiscrepanciesFoundFragment())
        }
    }

    override fun openJobCard(taskNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(JobCardFragment.create(taskNumber))
        }
    }

    override fun openLoadingTasksScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTasksFragment())
        }
    }

    override fun openConfirmationTaskOpenScreen(userName: String, ip: String, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirmation_task_open, userName, ip),
                    codeConfirm = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openLoadingTaskContentsScreen(taskInfo: TaskItemVm, recountType: RecountType) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTaskContentFragment.create(taskInfo, recountType))
        }
    }

    override fun openLoadingStorePlaceLockScreen(taskInfo: TaskItemVm, mode: StorePlaceLockMode, storePlaceInfo: TaskStorePlaceInfo)
    {
        runOrPostpone {
            getFragmentStack()?.push(LoadingStorePlaceLockFragment.create(taskInfo, mode, storePlaceInfo))
        }
    }


    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen()
    fun openMainMenuScreen()
    fun openGoodsInfoScreen(productInfo: TaskProductInfo)
    fun openGoodsDetailsStorageScreen(productInfo: TaskProductInfo)
    fun openGoodsListScreen()
    fun openSetsInfoScreen()
    fun openSetComponentsScreen()
    fun openStoragesList()
    fun openDiscrepanciesScreen()
    fun openTasksList()
    fun openJobCard(taskNumber: String)
    fun openLoadingTasksScreen()
    fun openLoadingTaskContentsScreen(taskInfo: TaskItemVm, recountType: RecountType)
    fun openLoadingStorePlaceLockScreen(taskInfo: TaskItemVm, mode: StorePlaceLockMode, storePlaceInfo: TaskStorePlaceInfo)
    fun openExciseAlcoInfoScreen()
    fun openConfirmationTaskOpenScreen(userName: String, ip: String, callbackFunc: () -> Unit)
}