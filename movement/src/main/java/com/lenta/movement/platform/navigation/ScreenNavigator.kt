package com.lenta.movement.platform.navigation

import android.content.Context
import com.lenta.movement.R
import com.lenta.movement.features.auth.AuthFragment
import com.lenta.movement.features.main.MainMenuFragment
import com.lenta.movement.features.loading.fast.FastDataLoadingFragment
import com.lenta.movement.features.main.box.GoodsListFragment
import com.lenta.movement.features.main.box.create.CreateBoxesFragment
import com.lenta.movement.features.selectmarket.SelectMarketFragment
import com.lenta.movement.features.selectpersonalnumber.SelectPersonnelNumberFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.models.core.ProductInfo
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

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

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

    override fun openCreateTask() {
        openNotImplementedScreenAlert("Карточка задания")
    }

    override fun openTaskList() {
        openNotImplementedScreenAlert("Задания на перемещение")
    }

    override fun openUnsavedDataDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                AlertFragment.create(
                message = context.getString(R.string.unsaved_data_will_lost),
                codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                iconRes = R.drawable.ic_delete_red_80dp,
                pageNumber = "80",
                leftButtonDecorationInfo = ButtonDecorationInfo.no,
                rightButtonDecorationInfo = ButtonDecorationInfo.yes))
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

    override fun openProductIncorrectForCreateBox(productInfo: ProductInfo) {
        openAlertScreen(
            message = context.getString(R.string.alert_product_incorrect_for_create_box, productInfo.materialNumber),
            iconRes = com.lenta.shared.R.drawable.is_warning_red_80dp
        )
    }

    override fun openCreateBoxByProduct(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(CreateBoxesFragment.newInstance(productInfo))
        }
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
    fun openCreateTask()
    fun openTaskList()
    fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int)
    fun openProductIncorrectForCreateBox(productInfo: ProductInfo)
    fun openCreateBoxByProduct(productInfo: ProductInfo)
}