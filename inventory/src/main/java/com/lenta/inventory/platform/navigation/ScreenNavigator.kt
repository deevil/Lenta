package com.lenta.inventory.platform.navigation

import android.content.Context
import androidx.core.content.ContextCompat
import com.lenta.inventory.R
import com.lenta.inventory.features.main_menu.MainMenuFragment
import com.lenta.inventory.features.auth.AuthFragment
import com.lenta.inventory.features.discrepancies_found.DiscrepanciesFoundFragment
import com.lenta.inventory.features.goods_details_storage.GoodsDetailsStorageFragment
import com.lenta.inventory.features.goods_information.excise_alco.ExciseAlcoInfoFragment
import com.lenta.inventory.features.goods_information.excise_alco.party_signs.PartySignsFragment
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
import com.lenta.inventory.features.taken_to_work.TakenToWorkFragment
import com.lenta.inventory.features.task_list.TaskListFragment
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.StorePlaceProcessing
import com.lenta.inventory.models.task.ProcessExciseAlcoProductService
import com.lenta.inventory.models.task.TaskStorePlaceInfo
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.progress.IInventoryProgressUseCaseInformator
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo

class ScreenNavigator(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IInventoryProgressUseCaseInformator
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

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        runOrPostpone {
            showProgress(progressUseCaseInformator.getTitle(useCase))
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

    override fun openExciseAlcoInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoInfoFragment.create(productInfo))
        }
    }

    override fun openPartySignsScreen(title: String, manufacturers: List<String>, stampLength: Int) {
        runOrPostpone {
            getFragmentStack()?.push(PartySignsFragment.create(title, manufacturers, stampLength))
        }
    }

    override fun openGoodsDetailsStorageScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsDetailsStorageFragment.create(productInfo))
        }
    }

    override fun openGoodsListScreen(storePlaceManager: StorePlaceProcessing) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment.create(storePlaceManager))
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

    override fun openLoadingTaskContentsScreen(taskInfo: TasksItem, recountType: RecountType) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTaskContentFragment.create(taskInfo, recountType))
        }
    }

    override fun openLoadingStorePlaceLockScreen(mode: StorePlaceLockMode, storePlaceNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingStorePlaceLockFragment.create(mode, storePlaceNumber))
        }
    }

    override fun openTakenToWorkFragment() {
        runOrPostpone {
            getFragmentStack()?.push(TakenToWorkFragment.create())
        }
    }

    override fun openAlertDoubleScanStamp() {
        openAlertScreen(
                message = context.getString(R.string.alert_double_scan_stamp),
                iconRes = R.drawable.ic_info_pink,
                textColor = ContextCompat.getColor(context, com.lenta.shared.R.color.color_text_dialogWarning),
                pageNumber = "98"
        )
    }

    override fun openConfirmationSavingJobScreen(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirmation_saving_job),
                    codeConfirm = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openSuccessSaveDataScreen() {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            iconRes = R.drawable.ic_done_green_80dp,
                            message = context.getString(R.string.success_save_report),
                            timeAutoExitInMillis = 3000,
                            leftButtonDecorationInfo = ButtonDecorationInfo.empty
                    )
            )
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
    fun openGoodsListScreen(storePlaceManager: StorePlaceProcessing)
    fun openSetsInfoScreen()
    fun openSetComponentsScreen()
    fun openStoragesList()
    fun openDiscrepanciesScreen()
    fun openTasksList()
    fun openJobCard(taskNumber: String)
    fun openLoadingTasksScreen()
    fun openLoadingTaskContentsScreen(taskInfo: TasksItem, recountType: RecountType)
    fun openLoadingStorePlaceLockScreen(mode: StorePlaceLockMode, storePlaceNumber: String)
    fun openExciseAlcoInfoScreen(productInfo: TaskProductInfo)
    fun openConfirmationTaskOpenScreen(userName: String, ip: String, callbackFunc: () -> Unit)
    fun openAlertDoubleScanStamp()
    fun openPartySignsScreen(title: String, manufacturers: List<String>, stampLength: Int)
    fun openTakenToWorkFragment()
    fun openConfirmationSavingJobScreen(callbackFunc: () -> Unit)
    fun openSuccessSaveDataScreen()
}