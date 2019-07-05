package com.lenta.inventory.platform.navigation

import android.content.Context
import com.lenta.inventory.features.main_menu.MainMenuFragment
import com.lenta.inventory.features.auth.AuthFragment
import com.lenta.inventory.features.discrepancies_found.DiscrepanciesFoundFragment
import com.lenta.inventory.features.goods_details.GoodsDetailsFragment
import com.lenta.inventory.features.goods_details_storage.GoodsDetailsStorageFragment
import com.lenta.inventory.features.goods_information.general.GoodsInfoFragment
import com.lenta.inventory.features.goods_information.sets.SetsInfoFragment
import com.lenta.inventory.features.goods_information.sets.components.SetComponentsFragment
import com.lenta.inventory.features.goods_list.GoodsListFragment
import com.lenta.inventory.features.job_card.JobCardFragment
import com.lenta.inventory.features.loading.fast.FastDataLoadingFragment
import com.lenta.inventory.features.loading.tasks.LoadingTasksFragment
import com.lenta.inventory.features.select_market.SelectMarketFragment
import com.lenta.inventory.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.inventory.features.sets_details_storage.SetsDetailsStorageFragment
import com.lenta.inventory.features.storages_list.StoragesListFragment
import com.lenta.inventory.features.task_list.TaskListFragment
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

    override fun openGoodsDetailsStorageScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsDetailsStorageFragment())
        }
    }

    override fun openSetsDetailsStorageScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SetsDetailsStorageFragment())
        }
    }

    override fun openGoodsListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment())
        }
    }

    override fun openSetsInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SetsInfoFragment())
        }
    }

    override fun openSetComponentsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SetComponentsFragment())
        }
    }

    override fun openStoragesList() {
        runOrPostpone {
            getFragmentStack()?.push(StoragesListFragment())
        }
    }

    override fun openTasksList() {
        runOrPostpone {
            getFragmentStack()?.push(TaskListFragment())
        }
    }

    override fun openDiscrepanciesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DiscrepanciesFoundFragment())
        }
    }

    override fun openJobCard() {
        runOrPostpone {
            getFragmentStack()?.push(JobCardFragment())
        }
    }

    override fun openLoadingTasksScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTasksFragment())
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
    fun openGoodsDetailsStorageScreen()
    fun openSetsDetailsStorageScreen()
    fun openGoodsListScreen()
    fun openSetsInfoScreen()
    fun openSetComponentsScreen()
    fun openStoragesList()
    fun openDiscrepanciesScreen()
    fun openTasksList()
    fun openJobCard()
    fun openLoadingTasksScreen()
}