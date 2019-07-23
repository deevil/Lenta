package com.lenta.bp7.platform.navigation

import android.content.Context
import com.lenta.bp7.features.auth.AuthFragment
import com.lenta.bp7.features.check_type.CheckTypeFragment
import com.lenta.bp7.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp7.features.option_info.OptionInfoFragment
import com.lenta.bp7.features.code.CodeFragment
import com.lenta.bp7.features.good_info.GoodInfoFragment
import com.lenta.bp7.features.good_info_facing.GoodInfoFacingFragment
import com.lenta.bp7.features.good_list.GoodListFragment
import com.lenta.bp7.features.segment_list.SegmentListFragment
import com.lenta.bp7.features.select_market.SelectMarketFragment
import com.lenta.bp7.features.shelf_list.ShelfListFragment

import com.lenta.shared.account.IAuthenticator
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

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun closeAllScreen() {
        runOrPostpone {
            getFragmentStack()?.popAll()
        }
    }

    override fun openMainMenuScreen() {
        openNotImplementedScreenAlert("Главное меню")
    }

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
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

    override fun openCheckTypeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(CheckTypeFragment())
        }
    }

    override fun openCodeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(CodeFragment())
        }
    }

    override fun openOptionScreen() {
        runOrPostpone {
            getFragmentStack()?.push(OptionInfoFragment())
        }
    }

    override fun openSegmentListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SegmentListFragment())
        }
    }

    override fun openShelfListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ShelfListFragment())
        }
    }

    override fun openGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodListFragment())
        }
    }

    override fun openGoodInfoFacingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFacingFragment())
        }
    }

    override fun openGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment())
        }
    }
}

interface IScreenNavigator : ICoreNavigator {
    fun closeAllScreen()
    fun openFirstScreen()
    fun openLoginScreen()
    fun openMainMenuScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openCheckTypeScreen()
    fun openCodeScreen()
    fun openOptionScreen()
    fun openSegmentListScreen()
    fun openShelfListScreen()
    fun openGoodListScreen()
    fun openGoodInfoFacingScreen()
    fun openGoodInfoScreen()

}