package com.lenta.shared.platform.navigation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.lenta.shared.R
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.analytics.IAnalytics
import com.lenta.shared.analytics.db.RoomAppDatabase
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.shared.features.exit.ExitWithConfirmationFragment
import com.lenta.shared.features.fmp_settings.FmpSettingsFragment
import com.lenta.shared.features.matrix_info.MatrixInfoFragment
import com.lenta.shared.features.printer_address.EnterPrinterAddressFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.shared.features.section_info.SectionInfoFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeViewModel
import com.lenta.shared.features.settings.SettingsFragment
import com.lenta.shared.features.support.SupportFragment
import com.lenta.shared.features.tech_login.TechLoginFragment
import com.lenta.shared.features.test_environment.PinCodeFragment
import com.lenta.shared.features.test_environment.failure.FailurePinCodeFragment
import com.lenta.shared.features.weight_equipment_name.WeightEquipmentNameFragment
import com.lenta.shared.fmp.resources.dao_ext.IconCode
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.pictogram.IIconDescriptionHelper
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.getApplicationName
import com.lenta.shared.utilities.extentions.openAnotherApp
import com.lenta.shared.utilities.extentions.restartApp
import com.lenta.shared.utilities.extentions.setFragmentResultCode
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject
import kotlin.system.exitProcess


class CoreNavigator @Inject constructor(
        private val context: Context,
        private val hyperHive: HyperHive,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val failureInterpreter: IFailureInterpreter,
        private val analytics: IAnalytics,
        private val analyticsHelper: AnalyticsHelper,
        private val roomAppDatabase: RoomAppDatabase,
        override val backFragmentResultHelper: BackFragmentResultHelper,
        private val iconDescriptionHelper: IIconDescriptionHelper
) : ICoreNavigator {


    override val functionsCollector: FunctionsCollector by lazy {
        FunctionsCollector(foregroundActivityProvider.onPauseStateLiveData)
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun goBackWithArgs(args: Bundle) {
        runOrPostpone {
            getFragmentStack()?.popReturnArgs(args = args)
        }
    }

    override fun goBackWithResultCode(code: Int?) {
        if (code == null) {
            backFragmentResultHelper.getFuncAndClear(null)
            goBack()
        } else {
            goBackWithArgs(
                    args = Bundle().apply {
                        setFragmentResultCode(code)
                    })
        }

    }

    override fun goBack() {
        runOrPostpone {
            analyticsHelper.onGoBack()
            getFragmentStack()?.pop()
        }
    }

    override fun finishApp(restart: Boolean) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.finish()
            analytics.cleanLogs()
            roomAppDatabase.close()
            hyperHive.databaseAPI.closeDefaultBase()
            hyperHive.authAPI.unAuth()
            if (restart) {
                context.restartApp()
            } else {
                exitProcess(0)
            }
        }

    }

    override fun openAlertScreen(message: String,
                                 iconRes: Int,
                                 textColor: Int?,
                                 pageNumber: String?,
                                 timeAutoExitInMillis: Int?,
                                 onlyIfFirstAlert: Boolean) {
        runOrPostpone {
            getFragmentStack()?.let {

                if (onlyIfFirstAlert && it.peek() is AlertFragment) {
                    return@let
                }

                val fragment = AlertFragment.create(
                        message = message,
                        iconRes = iconRes,
                        textColor = textColor,
                        pageNumber = pageNumber,
                        timeAutoExitInMillis = timeAutoExitInMillis
                )
                it.push(fragment, CustomAnimation.vertical)

            }
        }
    }

    override fun openAlertScreen(failure: Failure, pageNumber: String, timeAutoExitInMillis: Int?) {
        openAlertScreen(
                message = failureInterpreter.getFailureDescription(failure).message,
                iconRes = failureInterpreter.getFailureDescription(failure).iconRes,
                textColor = failureInterpreter.getFailureDescription(failure).textColor,
                timeAutoExitInMillis = timeAutoExitInMillis,
                pageNumber = pageNumber)
    }

    override fun openSupportScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SupportFragment())
        }
    }

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        showProgressLoadingData()
    }

    override fun showProgress(title: String) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.showSimpleProgress(title)
        }
    }

    override fun showProgressLoadingData() {
        runOrPostpone {
            showProgress(context.getString(R.string.data_loading))
        }
    }

    override fun showProgressConnection() {
        runOrPostpone {
            showProgress(context.getString(R.string.connection_setup))
        }
    }

    override fun hideProgress() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.hideProgress()
        }
    }

    override fun openTechLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TechLoginFragment())
        }
    }

    override fun openConnectionsSettingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FmpSettingsFragment())
        }
    }

    override fun openPinCodeScreen(requestCode: Int, message: String) {
        runOrPostpone {
            getFragmentStack()?.push(PinCodeFragment.create(requestCode, message))
        }
    }

    override fun openPinCodeForTestEnvironment() {
        runOrPostpone {
            getFragmentStack()?.push(
                    PinCodeFragment.create(
                            SelectOperModeViewModel.REQUEST_CODE_TEST_ENVIRONMENT,
                            context.getString(R.string.tv_test_envir)))
        }
    }

    override fun openSelectOperModeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectOperModeFragment())
        }
    }

    override fun openPrinterChangeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PrinterChangeFragment())
        }
    }

    override fun openEnterPrinterAddressScreen() {
        runOrPostpone {
            getFragmentStack()?.push(EnterPrinterAddressFragment())
        }
    }

    override fun openWeightEquipmentNameScreen() {
        runOrPostpone {
            getFragmentStack()?.push(WeightEquipmentNameFragment())
        }
    }

    override fun openSettingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SettingsFragment())
        }
    }

    override fun openAuxiliaryMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuxiliaryMenuFragment())
        }
    }

    override fun openFailurePinCodeScreen(message: String) {
        runOrPostpone {
            getFragmentStack()?.push(FailurePinCodeFragment.create(message = message))
        }
    }

    override fun openExitConfirmationScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExitWithConfirmationFragment())
        }
    }

    override fun openMatrixInfoScreen(matrixType: MatrixType) {
        runOrPostpone {
            getFragmentStack()?.push(MatrixInfoFragment.create(matrixType), CustomAnimation.vertical)
        }
    }

    override fun openSectionInfoScreen(section: String) {
        runOrPostpone {
            getFragmentStack()?.push(SectionInfoFragment.create(sectionNumber = section), CustomAnimation.vertical)
        }
    }

    override fun openEanInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = iconDescriptionHelper.getDescription(IconCode.EAN)
                    ?: context.getString(R.string.ean_info),
                    iconRes = R.drawable.ic_scan_barcode_48dp), CustomAnimation.vertical)
        }
    }

    override fun openQrCodeInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(message = iconDescriptionHelper.getDescription(IconCode.QR_CODE)
                            ?: context.getString(R.string.qr_code_info),
                            iconRes = R.drawable.ic_scan_qrcode_48dp), CustomAnimation.vertical)
        }
    }

    override fun openESInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = iconDescriptionHelper.getDescription(IconCode.EXCISE_STAMP)
                    ?: context.getString(R.string.es_info),
                    iconRes = R.drawable.ic_scan_barcode_es_48dp), CustomAnimation.vertical)
        }
    }

    override fun openGS128InfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.gs128_info),
                    iconRes = R.drawable.ic_scan_barcode_vet_48dp), CustomAnimation.vertical)
        }
    }

    override fun openBoxInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = iconDescriptionHelper.getDescription(IconCode.BOX_SCAN)
                    ?: context.getString(R.string.box_info),
                    iconRes = R.drawable.is_scan_box), CustomAnimation.vertical)
        }
    }

    override fun openInfoScreen(message: String) {
        openAlertScreen(message = message,
                iconRes = R.drawable.ic_info_pink,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "97"
        )
    }

    override fun openStampAnotherMarketAlert(codeConfirm: Int) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = context.getString(R.string.another_market_stamp),
                            pageNumber = "93",
                            codeConfirmForRight = codeConfirm,
                            rightButtonDecorationInfo = ButtonDecorationInfo.next
                    )
            )
        }
    }

    override fun openWriteOffToProductionConfirmationScreen(codeConfirm: Int) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = context.getString(R.string.writeoff_to_production_confirmation),
                            pageNumber = "95",
                            codeConfirmForRight = codeConfirm,
                            rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate
                    )
            )
        }
    }


    override fun openAnotherProductStampAlert(productName: String) {
        openInfoScreen(message = context.getString(R.string.another_product_stamp, productName))
    }

    override fun openNeedUpdateScreen() {
        openInfoScreen(context.getString(R.string.need_update))
    }

    override fun openNotImplementedScreenAlert(screenName: String) {
        openInfoScreen(context.getString(R.string.not_implemented_screen, screenName))

    }

    override fun closeAllScreen() {
        runOrPostpone {
            getFragmentStack()?.popAll()
        }
    }

    override fun openAlertAnotherAppInProcess(packageName: String) {
        runOrPostpone {

            backFragmentResultHelper.setFuncForResult {
                context.openAnotherApp(packageName)
                finishApp()
            }.let { code ->
                getFragmentStack()?.push(
                        AlertFragment.create(
                                message = context.getString(R.string.another_app_need_close, context.getApplicationName(packageName)),
                                codeConfirmForRight = code,
                                codeConfirmForExit = code,
                                pageNumber = "94",
                                leftButtonDecorationInfo = ButtonDecorationInfo.empty,
                                rightButtonDecorationInfo = ButtonDecorationInfo.yes
                        )
                )
            }


        }
    }

    override fun showUnsavedDataDetected(confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsaved_data_detected),
                    pageNumber = "80",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun openDetectedSavedDataScreen(deleteCallback: () -> Unit, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.saved_data_detect_message),
                    iconRes = R.drawable.ic_question_80dp,
                    pageNumber = "91",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm,
                    buttonDecorationInfo3 = ButtonDecorationInfo.delete))
        }

    }

    override fun openChangedDefaultSettingsAlert(noCallback: () -> Unit, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.detect_changes_connection_message),
                    iconRes = R.drawable.is_warning_yellow_80dp,
                    pageNumber = "91",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(noCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes,
                    buttonDecorationInfo3 = ButtonDecorationInfo.no))
        }
    }

    override fun showAlertBlockedTaskAnotherUser(userName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.another_user_block_task, userName),
                    iconRes = R.drawable.ic_info_pink,
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back
            )
            )
        }
    }

    override fun showAlertBlockedTaskAnotherUser(userName: String, deviceIp: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.task_block_user_with_tsd_ip, userName, deviceIp),
                    iconRes = R.drawable.ic_info_pink,
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back
            )
            )
        }
    }

    override fun showAlertBlockedTaskByMe(userName: String, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.user_self_block_task, userName),
                    iconRes = R.drawable.ic_question_80dp,
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback)
            )
            )
        }
    }

}

fun ICoreNavigator.runOrPostpone(function: () -> Unit) {
    functionsCollector.executeFunction(function)
}

interface ICoreNavigator {
    val functionsCollector: FunctionsCollector
    val backFragmentResultHelper: BackFragmentResultHelper
    fun goBackWithArgs(args: Bundle)
    fun goBackWithResultCode(code: Int?)
    fun goBack()
    fun finishApp(restart: Boolean = false)
    fun openAlertScreen(message: String,
                        iconRes: Int = 0,
                        textColor: Int? = null,
                        pageNumber: String? = null,
                        timeAutoExitInMillis: Int? = null,
                        onlyIfFirstAlert: Boolean = false)

    fun openAlertScreen(failure: Failure, pageNumber: String = "96", timeAutoExitInMillis: Int? = null)
    fun openSupportScreen()
    fun <Params> showProgress(useCase: UseCase<Any, Params>)
    fun showProgress(title: String)
    fun showProgressLoadingData()
    fun hideProgress()
    fun openTechLoginScreen()
    fun openConnectionsSettingsScreen()
    fun openPinCodeScreen(requestCode: Int, message: String)
    fun openPinCodeForTestEnvironment()
    fun openSelectOperModeScreen()
    fun openPrinterChangeScreen()
    fun openEnterPrinterAddressScreen()
    fun openWeightEquipmentNameScreen()
    fun openSettingsScreen()
    fun openAuxiliaryMenuScreen()
    fun openFailurePinCodeScreen(message: String)
    fun openExitConfirmationScreen()
    fun openMatrixInfoScreen(matrixType: MatrixType)
    fun openSectionInfoScreen(section: String)
    fun openEanInfoScreen()
    fun openQrCodeInfoScreen()
    fun openESInfoScreen()
    fun openBoxInfoScreen()
    fun openInfoScreen(message: String)
    fun openStampAnotherMarketAlert(codeConfirm: Int)
    fun openAnotherProductStampAlert(productName: String)
    fun openWriteOffToProductionConfirmationScreen(codeConfirm: Int)
    fun openNeedUpdateScreen()
    fun openNotImplementedScreenAlert(screenName: String)
    fun closeAllScreen()
    fun openAlertAnotherAppInProcess(packageName: String)
    fun showUnsavedDataDetected(confirmCallback: () -> Unit)
    fun openDetectedSavedDataScreen(deleteCallback: () -> Unit, confirmCallback: () -> Unit)
    fun openChangedDefaultSettingsAlert(noCallback: () -> Unit, yesCallback: () -> Unit)
    fun showProgressConnection()
    fun showAlertBlockedTaskAnotherUser(userName: String)
    fun showAlertBlockedTaskAnotherUser(userName: String, deviceIp: String)
    fun showAlertBlockedTaskByMe(userName: String, yesCallback: () -> Unit)
    fun openGS128InfoScreen()
}

class FunctionsCollector(private val needCollectLiveData: LiveData<Boolean>) {

    private val functions: MutableList<() -> Unit> = mutableListOf()

    init {
        Handler(Looper.getMainLooper()).post {
            needCollectLiveData.observeForever { needCollect ->
                if (!needCollect) {
                    functions.map { it }.forEach {
                        it()
                        functions.remove(it)
                    }
                }
            }
        }
    }

    fun executeFunction(func: () -> Unit) {
        if (needCollectLiveData.value == true) {
            functions.add(func)
        } else {
            func()
        }
    }


}