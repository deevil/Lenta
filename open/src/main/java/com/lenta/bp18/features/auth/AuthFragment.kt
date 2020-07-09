package com.lenta.bp18.features.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lenta.bp18.R
import com.lenta.bp18.platform.extention.getAppComponent
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.CoreLoginFragment
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class AuthFragment : CoreLoginFragment() {

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): CoreAuthViewModel {
        provideViewModel(AuthViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.packageName.value = context!!.packageName
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = context?.getAppInfo()
        topToolbarUiModel.description.value = getString(R.string.authorization)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.appTitle.value = context?.getAppInfo(withHash = false)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            com.lenta.shared.R.id.b_5 -> vm.onClickEnter()
            com.lenta.shared.R.id.b_topbar_1 -> vm.onClickAuxiliaryMenu()
        }
    }

    companion object {
        const val SCREEN_NUMBER = "1"
    }

}