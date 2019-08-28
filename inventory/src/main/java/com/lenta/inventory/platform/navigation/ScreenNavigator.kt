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
import com.lenta.inventory.features.goods_information.sets.SetComponentInfo
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
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.progress.IInventoryProgressUseCaseInformator
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.interactor.UseCase
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

    override fun openGoodsInfoScreen(productInfo: TaskProductInfo, initialCount: Double) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment.create(productInfo, initialCount))
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

    override fun openGoodsListScreen(storePlaceNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment.create(storePlaceNumber))
        }
    }

    override fun openSetsInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(SetsInfoFragment.create(productInfo))
        }
    }

    override fun openSetComponentsScreen(componentInfo: SetComponentInfo, isStamp: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(SetComponentsFragment.create(componentInfo, isStamp))
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
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
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
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationSkippingDiscrepancies(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirmation_skipping_discrepancies),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationSkippingDiscrepanciesRecount(rightCallbackFunc: () -> Unit, middleCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirmation_skipping_discrepancies_recount),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(rightCallbackFunc),
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(middleCallbackFunc),
                    pageNumber = "94",
                    rightButtonDecorationInfo = ButtonDecorationInfo.counted,
                    buttonDecorationInfo3 = ButtonDecorationInfo.published))
        }
    }

    override fun openConfirmationClean(byStorage: Boolean, callbackFunc: () -> Unit) {
        runOrPostpone {
            val message = if (byStorage) context.getString(R.string.confirmation_clean_storages) else context.getString(R.string.confirmation_clean_goods)
            getFragmentStack()?.push(AlertFragment.create(message = message,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationMissingGoods(positionsCount: Int, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirmation_missing_goods, positionsCount),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationTakeStorePlace(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.take_store_place),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationDeleteGoods(positionsCount: Int, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirmation_delete_goods, positionsCount),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }


    override fun openSuccessSaveDataScreen(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            iconRes = R.drawable.ic_done_green_80dp,
                            message = context.getString(R.string.success_save_report),
                            timeAutoExitInMillis = 3000,
                            leftButtonDecorationInfo = ButtonDecorationInfo.empty,
                            codeConfirmForExit = backFragmentResultHelper.setFuncForResult(callbackFunc)
                    )
            )
        }
    }

    override fun openConfirmationExitTask(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.confirmation_exit_task),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            )
            )
        }
    }

    override fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.select_type_code_description),
                    iconRes = 0,
                    codeConfirmForRight = codeConfirmationForBarCode,
                    codeConfirmForLeft = codeConfirmationForSap,
                    pageNumber = "90",
                    leftButtonDecorationInfo = ButtonDecorationInfo.sap,
                    rightButtonDecorationInfo = ButtonDecorationInfo.barcode)
            )
        }
    }

    override fun openAlertGoodsNotForTaskScreen() {
        openInfoScreen(context.getString(R.string.goods_not_for_task))
    }

    override fun openAlertWrongGoodsType() {
        openInfoScreen(context.getString(R.string.alco_forbidden))
    }

    override fun openMinUpdateSalesDialogScreen(minUpdSales: Long, functionForLeft: () -> Unit, functionForRight: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.min_update_sells_alert, minUpdSales),
                    iconRes = 0,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(functionForLeft),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(functionForRight),
                    pageNumber = "93",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next)
            )
        }
    }

    override fun openAlertStampOverload(message: String, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = message,
                            iconRes = R.drawable.ic_info_pink,
                            textColor = ContextCompat.getColor(context, com.lenta.shared.R.color.color_text_dialogWarning),
                            pageNumber = "98",
                            isVisibleLeftButton = false,
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                            rightButtonDecorationInfo = ButtonDecorationInfo.next
                    )
            )
        }
    }

    override fun openAlertInfoScreen(message: String) {
        openAlertScreen(message = message,
                iconRes = R.drawable.ic_info_pink,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "98"
        )
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
    fun openGoodsInfoScreen(productInfo: TaskProductInfo, initialCount: Double = 0.0)
    fun openGoodsDetailsStorageScreen(productInfo: TaskProductInfo)
    fun openGoodsListScreen(storePlaceNumber: String)
    fun openSetsInfoScreen(productInfo: TaskProductInfo)
    fun openSetComponentsScreen(componentInfo: SetComponentInfo, isStamp: Boolean)
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
    fun openConfirmationSkippingDiscrepancies(callbackFunc: () -> Unit)
    fun openConfirmationSkippingDiscrepanciesRecount(rightCallbackFunc: () -> Unit, middleCallbackFunc: () -> Unit)
    fun openConfirmationMissingGoods(positionsCount: Int, callbackFunc: () -> Unit)
    fun openConfirmationDeleteGoods(positionsCount: Int, callbackFunc: () -> Unit)
    fun openConfirmationClean(byStorage: Boolean = false, callbackFunc: () -> Unit)
    fun openConfirmationTakeStorePlace(callbackFunc: () -> Unit)
    fun openSuccessSaveDataScreen(callbackFunc: () -> Unit)
    fun openConfirmationExitTask(callbackFunc: () -> Unit)
    fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int)
    fun openAlertGoodsNotForTaskScreen()
    fun openAlertWrongGoodsType()
    fun openAlertStampOverload(message: String, callbackFunc: () -> Unit)
    fun openAlertInfoScreen(message: String)
    fun openMinUpdateSalesDialogScreen(minUpdSales: Long, functionForLeft: () -> Unit, functionForRight: () -> Unit)
}