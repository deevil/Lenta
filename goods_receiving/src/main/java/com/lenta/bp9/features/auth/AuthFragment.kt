package com.lenta.bp9.features.auth

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.bp9.platform.extentions.getAppTitle
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.CoreLoginFragment
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AuthFragment : CoreLoginFragment() {

    override fun getPageNumber(): String = "9/01"

    override fun getViewModel(): CoreAuthViewModel {
        provideViewModel(AuthViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.msgUserNoRights.value = getString(R.string.user_no_rights)
            /**it.msgErrorRegisteringServer.value = getString(R.string.error_registering_server)*/
            it.iconRes.value = R.drawable.ic_info_pink
            it.textColor.value = ContextCompat.getColor(context!!, com.lenta.shared.R.color.color_text_dialogWarning)
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