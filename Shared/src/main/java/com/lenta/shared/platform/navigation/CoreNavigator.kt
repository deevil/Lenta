package com.lenta.shared.platform.navigation

import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.lenta.shared.R
import com.lenta.shared.analytics.IAnalytics
import com.lenta.shared.analytics.db.RoomAppDatabase
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.shared.features.exit.ExitWithConfirmationFragment
import com.lenta.shared.features.fmp_settings.FmpSettingsFragment
import com.lenta.shared.features.matrix_info.MatrixInfoFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.shared.features.section_info.SectionInfoFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeViewModel
import com.lenta.shared.features.settings.SettingsFragment
import com.lenta.shared.features.support.SupportFragment
import com.lenta.shared.features.tech_login.TechLoginFragment
import com.lenta.shared.features.test_environment.PinCodeFragment
import com.lenta.shared.features.test_environment.failure.FailurePinCodeFragment
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.setFragmentResultCode
import kotlin.system.exitProcess


class CoreNavigator constructor(private val context: Context,
                                private val foregroundActivityProvider: ForegroundActivityProvider,
                                private val failureInterpreter: IFailureInterpreter,
                                private val analytics: IAnalytics,
                                private val roomAppDatabase: RoomAppDatabase,
                                override val backFragmentResultHelper: BackFragmentResultHelper) : ICoreNavigator {

    override val functionsCollector: FunctionsCollector by lazy {
        FunctionsCollector(foregroundActivityProvider.onPauseStateLiveData)
    }

    override fun goBackWithArgs(args: Bundle) {
        runOrPostpone {
            getFragmentStack()?.popReturnArgs(args = args)
        }
    }

    override fun goBackWithResultCode(code: Int) {
        goBackWithArgs(
                args = Bundle().apply {
                    setFragmentResultCode(code)
                })
    }

    override fun goBack() {
        runOrPostpone {
            getFragmentStack()?.pop()
        }
    }

    override fun finishApp() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.finish()
            analytics.cleanLogs()
            roomAppDatabase.close()
            exitProcess(0)
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

    override fun openAlertScreen(failure: Failure, pageNumber: String) {
        openAlertScreen(
                message = failureInterpreter.getFailureDescription(failure).message,
                iconRes = failureInterpreter.getFailureDescription(failure).iconRes,
                textColor = failureInterpreter.getFailureDescription(failure).textColor,
                pageNumber = pageNumber)
    }

    override fun openSupportScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SupportFragment())
        }
    }

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        runOrPostpone {
            showProgress(context.getString(R.string.data_loading))
        }
    }

    override fun showProgress(title: String) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.showSimpleProgress(title)
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
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.ean_info),
                    iconRes = R.drawable.ic_scan_barcode), CustomAnimation.vertical)
        }
    }

    override fun openESInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.es_info),
                    iconRes = R.drawable.is_scan_barcode_es), CustomAnimation.vertical)
        }
    }

    override fun openBoxInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.box_info),
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
        //TODO изменить реализацию метода после создания экрана
        openInfoScreen(context.getString(R.string.not_implemented_screen, screenName))

    }

    override fun closeAllScreen() {
        runOrPostpone {
            getFragmentStack()?.popAll()
        }
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

fun ICoreNavigator.runOrPostpone(function: () -> Unit) {
    functionsCollector.executeFunction(function)
}

interface ICoreNavigator {
    val functionsCollector: FunctionsCollector
    val backFragmentResultHelper: BackFragmentResultHelper
    fun goBackWithArgs(args: Bundle)
    fun goBackWithResultCode(code: Int)
    fun goBack()
    fun finishApp()
    fun openAlertScreen(message: String,
                        iconRes: Int = 0,
                        textColor: Int? = null,
                        pageNumber: String? = null,
                        timeAutoExitInMillis: Int? = null,
                        onlyIfFirstAlert: Boolean = false)

    fun openAlertScreen(failure: Failure, pageNumber: String = "96")
    fun openSupportScreen()
    fun <Params> showProgress(useCase: UseCase<Any, Params>)
    fun showProgress(title: String)
    fun hideProgress()
    fun openTechLoginScreen()
    fun openConnectionsSettingsScreen()
    fun openPinCodeScreen(requestCode: Int, message: String)
    fun openPinCodeForTestEnvironment()
    fun openSelectOperModeScreen()
    fun openPrinterChangeScreen()
    fun openSettingsScreen()
    fun openAuxiliaryMenuScreen()
    fun openFailurePinCodeScreen(message: String)
    fun openExitConfirmationScreen()
    fun openMatrixInfoScreen(matrixType: MatrixType)
    fun openSectionInfoScreen(section: String)
    fun openEanInfoScreen()
    fun openESInfoScreen()
    fun openBoxInfoScreen()
    fun openInfoScreen(message: String)
    fun openStampAnotherMarketAlert(codeConfirm: Int)
    fun openAnotherProductStampAlert(productName: String)
    fun openWriteOffToProductionConfirmationScreen(codeConfirm: Int)
    fun openNeedUpdateScreen()
    fun openNotImplementedScreenAlert(screenName: String)
    fun closeAllScreen()
}

class FunctionsCollector(private val needCollectLiveData: LiveData<Boolean>) {

    private val functions: MutableList<() -> Unit> = mutableListOf()

    init {
        needCollectLiveData.observeForever { needCollect ->
            if (!needCollect) {
                functions.map { it }.forEach {
                    it()
                    functions.remove(it)
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