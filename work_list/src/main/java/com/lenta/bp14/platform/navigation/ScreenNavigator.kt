package com.lenta.bp14.platform.navigation

import android.content.Context
import com.lenta.bp14.features.auth.AuthFragment
import com.lenta.bp14.features.check_list.goods_list.GoodsListClFragment
import com.lenta.bp14.features.work_list.good_info.GoodInfoWlFragment
import com.lenta.bp14.features.job_card.JobCardFragment
import com.lenta.bp14.features.list_of_differences.ListOfDifferencesFragment
import com.lenta.bp14.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp14.features.main_menu.MainMenuFragment
import com.lenta.bp14.features.not_exposed.goods_list.GoodsListNeFragment
import com.lenta.bp14.features.not_exposed.good_info.GoodInfoNeFragment
import com.lenta.bp14.features.price_check.good_info.GoodInfoPcFragment
import com.lenta.bp14.features.price_check.goods_list.GoodsListPcFragment
import com.lenta.bp14.features.print_settings.PrintSettingsFragment
import com.lenta.bp14.features.report_result.ReportResultFragment
import com.lenta.bp14.features.select_market.SelectMarketFragment
import com.lenta.bp14.features.task_list.TaskListFragment
import com.lenta.bp14.features.work_list.details_of_goods.DetailsOfGoodsFragment
import com.lenta.bp14.features.work_list.expected_deliveries.ExpectedDeliveriesFragment
import com.lenta.bp14.features.work_list.goods_list.GoodsListWlFragment
import com.lenta.bp14.features.work_list.sales_of_goods.SalesOfGoodsFragment
import com.lenta.bp14.features.work_list.search_filter.SearchFilterWlFragment
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

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectMarketFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MainMenuFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskListFragment())
        }
    }

    override fun openJobCardScreen(taskNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(JobCardFragment.create(taskNumber = taskNumber))
        }
    }

    override fun openCheckListGoodsList() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListClFragment())
        }
    }

    override fun openListOfDifferencesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ListOfDifferencesFragment())
        }
    }

    override fun openReportResultScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ReportResultFragment())
        }
    }

    override fun openPrintSettingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PrintSettingsFragment())
        }
    }

    override fun openDetailsOfGoodsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DetailsOfGoodsFragment())
        }
    }

    override fun openGoodInfoWlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoWlFragment())
        }
    }

    override fun openGoodsListWlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListWlFragment())
        }
    }

    override fun openGoodInfoPcScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoPcFragment())
        }
    }

    override fun openGoodsListPcScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListPcFragment())
        }
    }

    override fun openExpectedDeliveriesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExpectedDeliveriesFragment())
        }
    }

    override fun openSearchFilterWlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SearchFilterWlFragment())
        }
    }

    override fun openSalesOfGoodsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SalesOfGoodsFragment())
        }
    }

    override fun openGoodsListNeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListNeFragment())
        }
    }

    override fun openGoodInfoNeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoNeFragment())
        }
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openTaskListScreen()
    fun openJobCardScreen(taskNumber: String)
    fun openCheckListGoodsList()
    fun openListOfDifferencesScreen()
    fun openReportResultScreen()
    fun openPrintSettingsScreen()
    fun openDetailsOfGoodsScreen()
    fun openGoodInfoWlScreen()
    fun openGoodsListWlScreen()
    fun openGoodInfoPcScreen()
    fun openGoodsListPcScreen()
    fun openSearchFilterWlScreen()
    fun openExpectedDeliveriesScreen()
    fun openSalesOfGoodsScreen()
    fun openGoodsListNeScreen()
    fun openGoodInfoNeScreen()
}