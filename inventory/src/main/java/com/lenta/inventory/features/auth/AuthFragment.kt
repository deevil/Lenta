package com.lenta.inventory.features.auth

import android.os.Bundle
import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.inventory.platform.extentions.getAppTitle
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.CoreLoginFragment
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AuthFragment : CoreLoginFragment() {

    override fun getPageNumber(): String = "11/01"

    override fun getViewModel(): CoreAuthViewModel {
        provideViewModel(AuthViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getAppTitle()
        topToolbarUiModel.description.value = getString(R.string.authorization)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.appTitle.value = getAppTitle()
    }




}