package com.lenta.shared.features.app_updates

import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class AppUpdateFragment : CoreFragment<com.lenta.shared.databinding.LayoutLoginBinding, AppUpdateViewModel>(), OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.layout_app_updates
    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("69")
    }

    override fun getViewModel(): AppUpdateViewModel {
        provideViewModel(AppUpdateViewModel::class.java).let {
            coreComponent.inject(it)
            it.appUpdateInstaller = provideFromParentToCoreProvider()?.getAppUpdateInstaller()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.hide()

    }

    override fun onBackPressed(): Boolean {
        return false
    }
}