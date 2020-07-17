package com.lenta.movement.platform.navigation

import android.content.Context
import androidx.core.content.ContextCompat
import com.lenta.movement.R
import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.features.auth.AuthFragment
import com.lenta.movement.features.loading.fast.FastDataLoadingFragment
import com.lenta.movement.features.main.MainMenuFragment
import com.lenta.movement.features.main.box.GoodsListFragment
import com.lenta.movement.features.main.box.create.CreateBoxesFragment
import com.lenta.movement.features.selectmarket.SelectMarketFragment
import com.lenta.movement.features.selectpersonalnumber.SelectPersonnelNumberFragment
import com.lenta.movement.features.task.TaskFragment
import com.lenta.movement.features.task.basket.TaskBasketFragment
import com.lenta.movement.features.task.basket.info.TaskBasketInfoFragment
import com.lenta.movement.features.task.eo.TaskEOMergeFragment
import com.lenta.movement.features.task.eo.eo_insides.TaskEOMergeEOInsidesFragment
import com.lenta.movement.features.task.eo.formedDocs.TaskEOMergeFormedDocsFragment
import com.lenta.movement.features.task.eo.ge_insides.TaskEOMergeGEInsidesFragment
import com.lenta.movement.features.task.goods.TaskGoodsFragment
import com.lenta.movement.features.task.goods.details.TaskGoodsDetailsFragment
import com.lenta.movement.features.task.goods.info.TaskGoodsInfoFragment
import com.lenta.movement.models.*
import com.lenta.movement.progress.IWriteOffProgressUseCaseInformator
import com.lenta.movement.requests.network.models.documentsToPrint.DocumentsToPrintDocument
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.exception.Failure
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
        private val progressUseCaseInformator: IWriteOffProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        showProgress(progressUseCaseInformator.getTitle(useCase))
    }

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openSelectMarketScreen()
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

    override fun openGoodsList() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment())
        }
    }

    override fun openTaskList() {
        openNotImplementedScreenAlert(TASK_TO_MOVE)  //ЕTODO Работа с заданиями
    }

    override fun openUnsavedDataDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = context.getString(R.string.unsaved_data_will_lost),
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                            iconRes = R.drawable.ic_delete_red_80dp,
                            pageNumber = OPEN_UNSAVED_DATA_DIALOG_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.no,
                            rightButtonDecorationInfo = ButtonDecorationInfo.yes
                    )
            )
        }
    }

    override fun openSelectTypeCodeScreen(
            codeConfirmationForSap: Int,
            codeConfirmationForBarCode: Int
    ) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = context.getString(R.string.select_type_code_description),
                            iconRes = 0,
                            codeConfirmForRight = codeConfirmationForBarCode,
                            codeConfirmForLeft = codeConfirmationForSap,
                            pageNumber = OPEN_SELECT_TYPE_CODE_SCREEN_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.sap,
                            rightButtonDecorationInfo = ButtonDecorationInfo.barcode
                    )
            )
        }
    }

    override fun openProductIncorrectForCreateBox(productInfo: ProductInfo) {
        openAlertScreen(
                message = context.getString(
                        R.string.alert_product_incorrect_for_create_box,
                        productInfo.materialNumber
                ),
                iconRes = com.lenta.shared.R.drawable.ic_warning_red_80dp
        )
    }

    override fun openCreateBoxByProduct(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(CreateBoxesFragment.newInstance(productInfo))
        }
    }

    override fun openAlertScreen(failure: Failure, pageNumber: String, timeAutoExitInMillis: Int?) {
        when (failure) {
            is InfoFailure -> openInfoScreen(failure.msg)
            else -> coreNavigator.openAlertScreen(failure)
        }
    }

    override fun openInfoScreen(message: String) {
        openAlertScreen(
                message = message,
                iconRes = com.lenta.shared.R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, com.lenta.shared.R.color.color_text_white),
                pageNumber = INFO_SCREEN_PAGE_NUMBER
        )
    }

    override fun openBoxRewriteDialog(msg: String, yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = msg,
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                            iconRes = R.drawable.ic_question_yellow_80dp,
                            pageNumber = OPEN_BOX_REWRITE_DIALOG_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.no,
                            rightButtonDecorationInfo = ButtonDecorationInfo.yes
                    )
            )
        }
    }

    override fun openStampMaxCountDialog() {
        openInfoScreen(context.getString(R.string.stamp_max_count))
    }

    override fun openStampWasAddedDialog(exciseBox: ExciseBox?) {
        if (exciseBox != null) {
            val shortBoxCode = "${exciseBox.code.take(5)}...${exciseBox.code.takeLast(5)}"

            openInfoScreen(context.getString(R.string.stamp_was_added_to_box_msg, shortBoxCode))
        } else {
            openInfoScreen(context.getString(R.string.stamp_was_added_msg))
        }
    }

    override fun openBoxNumberWasUsedDialog() {
        openInfoScreen(context.getString(R.string.box_number_was_used_msg))
    }

    override fun openEanInvalidDialog() {
        openInfoScreen(context.getString(R.string.ean_invalid_msg))
    }

    override fun openBoxSavedDialog(box: ExciseBox) {
        val shortBoxCode = "${box.code.take(5)}...${box.code.takeLast(5)}"
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = context.getString(R.string.box_saved_msg, shortBoxCode),
                            iconRes = R.drawable.ic_checkbox_green_32dp,
                            pageNumber = BOX_SAVED_DIALOG_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.back
                    )
            )
        }
    }

    override fun openTaskScreen(task: Task?) {
        runOrPostpone {
            getFragmentStack()?.push(TaskFragment.newInstance(task))
        }
    }

    override fun openTaskCompositionScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskGoodsFragment())
        }
    }

    override fun openTaskGoodsInfoScreen(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(TaskGoodsInfoFragment.newInstance(productInfo))
        }
    }

    override fun openTaskGoodsDetailsScreen(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(TaskGoodsDetailsFragment.newInstance(productInfo))
        }
    }

    override fun openTaskBasketScreen(basketIndex: Int) {
        runOrPostpone {
            getFragmentStack()?.push(TaskBasketFragment.newInstance(basketIndex))
        }
    }

    override fun openTaskBasketCharacteristicsScreen(basketIndex: Int) {
        runOrPostpone {
            getFragmentStack()?.push(TaskBasketInfoFragment.newInstance(basketIndex))
        }
    }

    override fun openSaveTaskConfirmationDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            description = context.getString(R.string.data_saving),
                            message = context.getString(R.string.task_save_confirmation_msg),
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                            iconRes = R.drawable.ic_question_yellow_80dp,
                            pageNumber = SAVE_TASK_CONFIRM_DIALOG_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.back,
                            rightButtonDecorationInfo = ButtonDecorationInfo.yes
                    )
            )
        }
    }

    override fun openTaskEoMergeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskEOMergeFragment())
        }
    }

    override fun openZeroSelectedEODialog(processCallbackFunc: () -> Unit, combineCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            description = context.getString(R.string.task_goods_title),
                            message = context.getString(R.string.task_eo_merge_zero_selected_eo_message),
                            iconRes = R.drawable.ic_question_yellow_80dp,
                            pageNumber = ZERO_SELECTED_EO_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.back,
                            buttonDecorationInfo3 = ButtonDecorationInfo(
                                    iconRes = R.drawable.ic_process_48dp,
                                    titleRes = R.string.process
                            ),
                            codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(processCallbackFunc),
                            rightButtonDecorationInfo = ButtonDecorationInfo(
                                    iconRes = R.drawable.ic_combine_48dp,
                                    titleRes = R.string.combine
                            ),
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(combineCallbackFunc)
                    )
            )
        }
    }

    override fun openTaskEoMergeFormedDocumentsScreen(docList: List<DocumentsToPrintDocument>) {
        runOrPostpone {
            getFragmentStack()?.push(TaskEOMergeFormedDocsFragment.newInstance(docList))
        }
    }

    override fun openTaskEoMergePrintConfirmationDialog(eoGeQuantity: Int, yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            description = context.getString(R.string.detected_discrepancies),
                            message = context.getString(R.string.task_eo_merge_print_confirmation_msg, eoGeQuantity),
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                            iconRes = R.drawable.ic_question_yellow_80dp,
                            pageNumber = SAVE_TASK_CONFIRM_DIALOG_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.back,
                            rightButtonDecorationInfo = ButtonDecorationInfo.yes
                    )
            )
        }
    }

    override fun openTaskEoMergePrintedDialog() {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            description = context.getString(R.string.cargoUnit_doc_print),
                            message = context.getString(R.string.cargoUnit_doc_printed),
                            iconRes = R.drawable.ic_done_green_80dp,
                            pageNumber = SAVE_TASK_CONFIRM_DIALOG_PAGE_NUMBER,
                            leftButtonDecorationInfo = ButtonDecorationInfo.back,
                            rightButtonDecorationInfo = ButtonDecorationInfo.next,
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult {
                                goBack()
                                goBack()
                            }
                    )
            )
        }
    }

    override fun openGEInsidesScreen(ge : CargoUnit) {
        runOrPostpone {
            getFragmentStack()
                    ?.push(
                            TaskEOMergeGEInsidesFragment.newInstance(ge)
                    )
        }
    }

    override fun openEOInsidesScreen(eo : ProcessingUnit) {
        runOrPostpone {
            getFragmentStack()
                    ?.push(
                            TaskEOMergeEOInsidesFragment.newInstance(eo)
                    )
        }
    }

    companion object {
        private const val TASK_TO_MOVE = "Задания на перемещение"
        private const val ZERO_SELECTED_EO_PAGE_NUMBER = "02"
        private const val SAVE_TASK_CONFIRM_DIALOG_PAGE_NUMBER = "06"
        private const val BOX_SAVED_DIALOG_PAGE_NUMBER = "02"
        private const val OPEN_BOX_REWRITE_DIALOG_PAGE_NUMBER = "80"
        private const val INFO_SCREEN_PAGE_NUMBER = "97"
        private const val OPEN_SELECT_TYPE_CODE_SCREEN_PAGE_NUMBER = "90"
        private const val OPEN_UNSAVED_DATA_DIALOG_PAGE_NUMBER = "80"
    }
}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen()
    fun openMainMenuScreen()
    fun openGoodsList()
    fun openUnsavedDataDialog(yesCallbackFunc: () -> Unit)
    fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int)
    fun openProductIncorrectForCreateBox(productInfo: ProductInfo)
    fun openCreateBoxByProduct(productInfo: ProductInfo)
    fun openBoxRewriteDialog(msg: String, yesCallbackFunc: () -> Unit)
    fun openStampMaxCountDialog()
    fun openStampWasAddedDialog(exciseBox: ExciseBox? = null)
    fun openBoxNumberWasUsedDialog()
    fun openEanInvalidDialog()
    fun openBoxSavedDialog(box: ExciseBox)
    fun openTaskScreen(task: Task?)
    fun openTaskCompositionScreen()
    fun openTaskGoodsInfoScreen(productInfo: ProductInfo)
    fun openTaskGoodsDetailsScreen(productInfo: ProductInfo)
    fun openTaskBasketScreen(basketIndex: Int)
    fun openTaskBasketCharacteristicsScreen(basketIndex: Int)
    fun openSaveTaskConfirmationDialog(yesCallbackFunc: () -> Unit)
    fun openTaskList()
    fun openTaskEoMergeScreen()
    fun openZeroSelectedEODialog(processCallbackFunc: () -> Unit, combineCallbackFunc: () -> Unit)
    fun openTaskEoMergeFormedDocumentsScreen(docList: List<DocumentsToPrintDocument>)
    fun openTaskEoMergePrintConfirmationDialog(eoGeQuantity: Int, yesCallbackFunc: () -> Unit)
    fun openTaskEoMergePrintedDialog()
    fun openGEInsidesScreen(ge : CargoUnit)
    fun openEOInsidesScreen(eo : ProcessingUnit)
}