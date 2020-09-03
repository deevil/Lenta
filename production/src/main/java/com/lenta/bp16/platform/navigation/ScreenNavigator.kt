package com.lenta.bp16.platform.navigation

import android.content.Context
import com.lenta.bp16.R
import com.lenta.bp16.features.auth.AuthFragment
import com.lenta.bp16.features.defect_info.DefectInfoFragment
import com.lenta.bp16.features.defect_list.DefectListFragment
import com.lenta.bp16.features.external_supply_list.ExternalSupplyListFragment
import com.lenta.bp16.features.external_supply_task_list.ExternalSupplyTaskListFragment
import com.lenta.bp16.features.good_info.GoodInfoFragment
import com.lenta.bp16.features.good_packaging.GoodPackagingFragment
import com.lenta.bp16.features.good_weighing.GoodWeighingFragment
import com.lenta.bp16.features.ingredient_details.IngredientDetailsFragment
import com.lenta.bp16.features.ingredients_list.IngredientsListFragment
import com.lenta.bp16.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp16.features.main_menu.MainMenuFragment
import com.lenta.bp16.features.material_remake_details.MaterialRemakeDetailsFragment
import com.lenta.bp16.features.material_remake_list.MaterialRemakesListFragment
import com.lenta.bp16.features.order_details.OrderDetailsFragment
import com.lenta.bp16.features.order_ingredients_list.OrderIngredientsListFragment
import com.lenta.bp16.features.pack_good_list.PackGoodListFragment
import com.lenta.bp16.features.pack_list.PackListFragment
import com.lenta.bp16.features.processing_unit_list.ProcessingUnitListFragment
import com.lenta.bp16.features.processing_unit_task_list.ProcessingUnitTaskListFragment
import com.lenta.bp16.features.raw_list.RawListFragment
import com.lenta.bp16.features.reprint_label.ReprintLabelFragment
import com.lenta.bp16.features.select_good.GoodSelectFragment
import com.lenta.bp16.features.select_market.SelectMarketFragment
import com.lenta.bp16.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.bp16.features.tech_orders_list.TechOrdersListFragment
import com.lenta.bp16.features.warehouse_selection.WarehouseSelectionFragment
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.model.pojo.GoodParams
import com.lenta.bp16.platform.Constants
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }


    // Основные экраны
    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectMarketFragment())
        }
    }

    override fun openSelectPersonnelNumberScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectPersonnelNumberFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MainMenuFragment())
        }
    }

    override fun openProcessingUnitTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ProcessingUnitTaskListFragment())
        }
    }

    override fun openExternalSupplyTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExternalSupplyTaskListFragment())
        }
    }

    override fun openSelectWarehouseScreen() {
        runOrPostpone {
            getFragmentStack()?.push(WarehouseSelectionFragment())
        }
    }

    override fun openProcessingUnitListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ProcessingUnitListFragment.newInstance())
        }
    }

    override fun openExternalSupplyListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExternalSupplyListFragment())
        }
    }

    override fun openRawListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(RawListFragment())
        }
    }

    override fun openGoodWeighingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodWeighingFragment())
        }
    }

    override fun openGoodPackagingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodPackagingFragment())
        }
    }

    override fun openPackListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PackListFragment())
        }
    }

    override fun openPackGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PackGoodListFragment())
        }
    }

    override fun openReprintLabelScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ReprintLabelFragment())
        }
    }

    override fun openDefectInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DefectInfoFragment())
        }
    }

    override fun openDefectListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DefectListFragment())
        }
    }

    override fun openIngredientsListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(IngredientsListFragment())
        }
    }

    override fun openSelectGoodScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodSelectFragment())
        }
    }

    override fun openGoodInfoScreen(goodParams: GoodParams) {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment.newInstance(goodParams))
        }
    }

    override fun openOrderDetailsScreen(selectedIngredient: IngredientInfo, barcode: OrderByBarcodeUI) {
        getFragmentStack()?.push(OrderDetailsFragment.newInstance(selectedIngredient, barcode))
    }

    override fun openOrderIngredientsListScreen(weight: String, selectedIngredient: IngredientInfo) {
        getFragmentStack()?.push(OrderIngredientsListFragment.newInstance(weight, selectedIngredient))
    }

    override fun openIngredientDetailsScreen(selectedIngredient: OrderIngredientDataInfo, parentCode: String, eanInfo: OrderByBarcodeUI) {
        getFragmentStack()?.push(IngredientDetailsFragment.newInstance(selectedIngredient, parentCode, eanInfo))
    }

    override fun openMaterialRemakesScreen(selectedIngredient: IngredientInfo) {
        getFragmentStack()?.push(MaterialRemakesListFragment.newInstance(selectedIngredient))
    }

    override fun openMaterialRemakeDetailsScreen(selectedMaterial: MaterialIngredientDataInfo, parentCode: String, parentName: String, barcode: OrderByBarcodeUI) {
        getFragmentStack()?.push(MaterialRemakeDetailsFragment.newInstance(selectedMaterial, parentCode, parentName, barcode))
    }

    override fun openTechOrdersScreen(selectedMaterial: MaterialIngredientDataInfo, parentCode: String, materialIngredientKtsch: String) {
        getFragmentStack()?.push(TechOrdersListFragment.newInstance(selectedMaterial, parentCode, materialIngredientKtsch))
    }

    // Информационные экраны
    override fun showDefrostingPhaseIsCompleted(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "12",
                    message = context.getString(R.string.defrosting_phase_is_completed),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
            ))
        }
    }

    override fun showFixStartNextStageSuccessful(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "12.1",
                    message = context.getString(R.string.fix_start_next_stage_successful),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
            ))
        }
    }

    override fun showConfirmNoSuchItemLeft(taskType: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "23",
                    message = context.getString(R.string.confirm_that_there_is_no_such_item_left, taskType),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm
            ))
        }
    }

    override fun showConfirmNoRawItem(taskType: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "24",
                    message = context.getString(R.string.confirm_no_raw_items, taskType),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm
            ))
        }
    }

    override fun showFixingPackagingPhaseSuccessful(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "35",
                    message = context.getString(R.string.fixing_beginning_of_packaging_phase_was_successful),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
            ))
        }
    }

    override fun showMoreThanOneOrderForThisProduct(backCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "32.1",
                    message = context.getString(R.string.more_than_one_order_for_this_product),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(backCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.back
            ))
        }
    }

    override fun showProcessOrderNotFound(backCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "32.2",
                    message = context.getString(R.string.process_order_not_found),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(backCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.back
            ))
        }
    }

    override fun showNotSavedDataWillBeLost(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "36",
                    message = context.getString(R.string.not_saved_data_will_be_lost),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showAlertNoIpPrinter() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "36",
                    message = context.getString(R.string.no_ip_printer_alert)
            ))
        }
    }

    override fun showLabelSentToPrint(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "99",
                    message = context.getString(R.string.label_sent_to_print),
                    iconRes = R.drawable.ic_info_green_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
            ))
        }
    }

    override fun showAlertPartNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_alert_part_not_found),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showAlertGoodNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_alert_good_not_found),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showAlertIngredientNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_alert_ingredient_not_found)
            ))
        }
    }

    override fun showNotFoundedBarcodeForPosition() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_alert_ingredient_not_found_in_position),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showAlertDualism() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_alert_dualism),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showAlertGoodNotFoundInCurrentShift() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_alert_good_not_found_in_current_shift),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showMovingSuccessful(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_moving_successful),
                    iconRes = R.drawable.ic_info_green_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    isVisibleLeftButton = false
            ))
        }
    }

    override fun showAlertExceededLimit(backCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = Constants.ALERT_FRAGMENT,
                    message = context.getString(R.string.tw_exceeded_limit),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(backCallback)
            ))
        }
    }

    override fun showAlertWeightNotSet() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.error_weight_not_set),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    pageNumber = Constants.ALERT_FRAGMENT
            ))
        }
    }

    override fun showAlertWrongDate() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_wrong_date),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    pageNumber = Constants.ALERT_FRAGMENT
            ))
        }
    }

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openSelectMarketScreen()
    fun openSelectPersonnelNumberScreen()
    fun openMainMenuScreen()
    fun openProcessingUnitTaskListScreen()
    fun openExternalSupplyTaskListScreen()
    fun openSelectWarehouseScreen()
    fun openProcessingUnitListScreen()
    fun openExternalSupplyListScreen()
    fun openRawListScreen()
    fun openGoodWeighingScreen()
    fun openGoodPackagingScreen()
    fun openPackListScreen()
    fun openPackGoodListScreen()
    fun openReprintLabelScreen()
    fun openDefectInfoScreen()
    fun openDefectListScreen()
    fun openGoodInfoScreen(goodParams: GoodParams)
    fun openSelectGoodScreen()
    fun openIngredientsListScreen()
    fun openOrderDetailsScreen(selectedIngredient: IngredientInfo, barcode: OrderByBarcodeUI)
    fun openIngredientDetailsScreen(selectedIngredient: OrderIngredientDataInfo, parentCode: String, eanInfo: OrderByBarcodeUI)
    fun openOrderIngredientsListScreen(weight: String, selectedIngredient: IngredientInfo)
    fun openMaterialRemakesScreen(selectedIngredient: IngredientInfo)
    fun openMaterialRemakeDetailsScreen(selectedMaterial: MaterialIngredientDataInfo, parentCode: String, parentName: String, barcode: OrderByBarcodeUI)
    fun openTechOrdersScreen(selectedMaterial: MaterialIngredientDataInfo, parentCode: String, materialIngredientKtsch: String)

    fun showDefrostingPhaseIsCompleted(nextCallback: () -> Unit)
    fun showFixStartNextStageSuccessful(nextCallback: () -> Unit)
    fun showConfirmNoSuchItemLeft(taskType: String, confirmCallback: () -> Unit)
    fun showConfirmNoRawItem(taskType: String, confirmCallback: () -> Unit)
    fun showFixingPackagingPhaseSuccessful(nextCallback: () -> Unit)
    fun showMoreThanOneOrderForThisProduct(backCallback: () -> Unit)
    fun showProcessOrderNotFound(backCallback: () -> Unit)
    fun showNotSavedDataWillBeLost(yesCallback: () -> Unit)
    fun showAlertNoIpPrinter()
    fun showLabelSentToPrint(nextCallback: () -> Unit)
    fun showAlertWeightNotSet()
    fun showAlertExceededLimit(backCallback: () -> Unit)
    fun showMovingSuccessful(nextCallback: () -> Unit)
    fun showAlertGoodNotFound()
    fun showAlertPartNotFound()
    fun showAlertDualism()
    fun showAlertGoodNotFoundInCurrentShift()
    fun showAlertIngredientNotFound()
    fun showNotFoundedBarcodeForPosition()
    fun showAlertWrongDate()
}