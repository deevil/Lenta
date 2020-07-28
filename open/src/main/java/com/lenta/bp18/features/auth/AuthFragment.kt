package com.lenta.bp18.features.auth

import android.os.Bundle
import android.view.View
import com.lenta.bp18.R
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.extention.getAppComponent
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.CoreLoginFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.setInvisible

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

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.enterToApp)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.appTitle.value = context?.getAppInfo(withHash = false)
        hideLoginAndPassword()
    }

    private fun hideLoginAndPassword() {
        binding?.layoutLogin?.apply {
            tvLogin.setInvisible()
            etLogin.setInvisible()
            tvPassword.setInvisible()
            etPassword.setInvisible()
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickEnter()
            R.id.b_topbar_1 -> vm.onClickAuxiliaryMenu()
        }
    }

    companion object {
        const val SCREEN_NUMBER = Constants.AUTH_FRAGMENT
    }

}