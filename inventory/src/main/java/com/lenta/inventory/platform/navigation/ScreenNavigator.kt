package com.lenta.inventory.platform.navigation

import android.content.Context
import com.lenta.inventory.features.main_menu.MainMenuFragment
import com.lenta.inventory.features.auth.AuthFragment
import com.lenta.inventory.features.goods_details.GoodsDetailsFragment
import com.lenta.inventory.features.goods_details_mx.GoodsDetailsMXFragment
import com.lenta.inventory.features.goods_information.general.GoodsInfoFragment
import com.lenta.inventory.features.loading.fast.FastDataLoadingFragment
import com.lenta.inventory.features.select_market.SelectMarketFragment
import com.lenta.inventory.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.inventory.features.sets_details_mx.SetsDetailsMXFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.matrix_info.MatrixInfoFragment
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.progress.IProgressUseCaseInformator

class ScreenNavigator(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
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

    override fun openGoodsInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment())
        }
    }

    override fun openGoodsDetailsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsDetailsFragment())
        }
    }

    override fun openGoodsDetailsMXScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsDetailsMXFragment())
        }
    }

    override fun openSetsDetailsMXScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SetsDetailsMXFragment())
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
    fun openGoodsInfoScreen()
    fun openGoodsDetailsScreen()
    fun openGoodsDetailsMXScreen()
    fun openSetsDetailsMXScreen()
}