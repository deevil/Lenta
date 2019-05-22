package com.lenta.bp10.platform.navigation

import android.content.Context
import com.lenta.bp10.R
import com.lenta.bp10.exception.IWriteOffFailureInterpretator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.bp10.features.auth.AuthFragment
import com.lenta.bp10.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.bp10.features.exit.ExitWithConfirmationFragment
import com.lenta.shared.features.fmp_settings.FmpSettingsFragment
import com.lenta.bp10.features.good_information.general.GoodInfoFragment
import com.lenta.bp10.features.good_information.sets.SetsFragment
import com.lenta.bp10.features.goods_list.GoodsListFragment
import com.lenta.bp10.features.job_card.JobCardFragment
import com.lenta.bp10.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp10.features.loading.tasks_settings.LoadingTaskSettingsFragment
import com.lenta.bp10.features.main_menu.MainMenuFragment
import com.lenta.bp10.features.matrix_info.MatrixInfoFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.bp10.features.report_result.ReportResultFragment
import com.lenta.bp10.features.section_info.SectionInfoFragment
import com.lenta.bp10.features.select_market.SelectMarketFragment
import com.lenta.bp10.features.select_oper_mode.SelectOperModeFragment
import com.lenta.bp10.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.bp10.features.settings.SettingsFragment
import com.lenta.shared.features.test_environment.TestEnvirFragment
import com.lenta.bp10.features.write_off_details.WriteOffDetailsFragment
import com.lenta.bp10.requests.network.WriteOffReportResponse
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.exception.Failure
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.lenta.shared.utilities.Logg

class ScreenNavigator(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val failureInterpreter: IWriteOffFailureInterpretator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    override fun openAlertScreen(failure: Failure) {
        openAlertScreen(failureInterpreter.getFailureDescription(failure))
    }


    override fun openSelectMarketScreen() {
        getFragmentStack()?.replace(SelectMarketFragment())
    }

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {

        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        Logg.d()
        getFragmentStack()?.let {
            it.popAll()
            it.replace(AuthFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        getFragmentStack()?.push(FastDataLoadingFragment())
    }

    override fun openSelectionPersonnelNumberScreen() {
        getFragmentStack()?.replace(SelectPersonnelNumberFragment())
    }

    override fun openAuxiliaryMenuScreen() {
        getFragmentStack()?.push(AuxiliaryMenuFragment())
    }


    override fun openSelectOperModeScreen() {
        getFragmentStack()?.push(SelectOperModeFragment())
    }

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        showProgress(progressUseCaseInformator.getTitle(useCase))
    }

    override fun openMainMenuScreen() {
        getFragmentStack()?.replace(MainMenuFragment())
    }

    override fun openJobCardScreen() {
        getFragmentStack()?.push(JobCardFragment())
    }

    override fun openLoadingTaskSettingsScreen() {
        getFragmentStack()?.push(LoadingTaskSettingsFragment())
    }

    override fun openSettingsScreen() {
        getFragmentStack()?.push(SettingsFragment())
    }

    override fun openGoodsListScreen() {
        getFragmentStack()?.push(GoodsListFragment())
    }

    override fun openGoodInfoScreen(productInfo: ProductInfo) {
        getFragmentStack()?.push(GoodInfoFragment.create(productInfo))
    }

    override fun openSetsInfoScreen(productInfo: ProductInfo) {
        getFragmentStack()?.push(SetsFragment.create(productInfo))
    }



    override fun openPrinterChangeScreen() {
        getFragmentStack()?.push(PrinterChangeFragment())
    }

    override fun openTestEnvirScreen() {
        getFragmentStack()?.push(TestEnvirFragment())
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun openEanInfoScreen() {
        getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.ean_info),
                iconRes = R.drawable.ic_scan_barcode))
    }

    override fun openExitConfirmationScreen() {
        getFragmentStack()?.push(ExitWithConfirmationFragment())
    }

    override fun openRemoveTaskConfirmationScreen(taskDescription: String, codeConfirmation: Int) {
        getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.remove_task_confirmation, taskDescription),
                iconRes = R.drawable.ic_delete_red_80dp, codeConfirm = codeConfirmation))
    }

    override fun openSendingReportsScreen(writeOffReportResponse: WriteOffReportResponse) {
        getFragmentStack()?.replace(ReportResultFragment.create(writeOffReportResponse))
    }

    override fun closeAllScreen() {
        getFragmentStack()?.popAll()
    }

    override fun openMatrixInfoScreen(matrixType: MatrixType) {
        getFragmentStack()?.push(MatrixInfoFragment.create(matrixType))
    }

    override fun openSectionInfoScreen(section: Int) {
        getFragmentStack()?.push(SectionInfoFragment.create(sectionNumber = "$section"))
    }

    override fun openGoodsReasonsScreen(productInfo: ProductInfo) {
        getFragmentStack()?.push(WriteOffDetailsFragment.create(productInfo))
    }

    override fun openConnectionsSettingsScreen() {
        getFragmentStack()?.push(FmpSettingsFragment())
    }
}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen()
    fun openAuxiliaryMenuScreen()
    fun openSelectOperModeScreen()
    fun openSettingsScreen()
    fun openMainMenuScreen()
    fun openJobCardScreen()
    fun openLoadingTaskSettingsScreen()
    fun openPrinterChangeScreen()
    fun openTestEnvirScreen()
    fun openGoodsListScreen()
    fun openGoodInfoScreen(productInfo: ProductInfo)
    fun openEanInfoScreen()
    fun openExitConfirmationScreen()
    fun openRemoveTaskConfirmationScreen(taskDescription: String, codeConfirmation: Int)
    fun openSendingReportsScreen(writeOffReportResponse: WriteOffReportResponse)
    fun closeAllScreen()
    fun openSetsInfoScreen(productInfo: ProductInfo)
    fun openMatrixInfoScreen(matrixType: MatrixType)
    fun openSectionInfoScreen(section: Int)
    fun openGoodsReasonsScreen(productInfo: ProductInfo)
    fun openConnectionsSettingsScreen()
}