package com.lenta.bp18.platform.navigation

import android.content.Context
import com.lenta.bp18.features.auth.AuthFragment
import com.lenta.bp18.features.goods_info.GoodsInfoFragment
import com.lenta.bp18.features.result.ResultFragment
import com.lenta.bp18.features.search.SearchFragment
import com.lenta.bp18.features.select_market.SelectMarketFragment
import com.lenta.bp18.features.sync.SyncFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
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

    override fun openAuthScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectMarketFragment())
        }
    }

    override fun openGoodsInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment())
        }
    }

    override fun openResultScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ResultFragment())
        }
    }

    override fun openSearchScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SearchFragment())
        }
    }

    override fun openSyncScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SyncFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        TODO("Not yet implemented")
    }

}

interface IScreenNavigator : ICoreNavigator {
    fun openAuthScreen()
    fun openSelectMarketScreen()
    fun openGoodsInfoScreen()
    fun openResultScreen()
    fun openSearchScreen()
    fun openSyncScreen()
    fun openFastDataLoadingScreen()
}