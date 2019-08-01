package com.lenta.bp7.features.auth

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp7.R
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.bp7.platform.extentions.getAppTitle
import com.lenta.shared.databinding.FragmentLoginBinding
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.setInvisible

class AuthFragment : CoreFragment<FragmentLoginBinding, AuthViewModel>(),
        OnBackPresserListener, ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = com.lenta.shared.R.layout.fragment_login

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("01")

    override fun getViewModel(): AuthViewModel {
        provideViewModel(AuthViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getAppTitle()
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton5.let { buttonUiModel ->
            buttonUiModel.show(ButtonDecorationInfo.enterToApp)
            vm.enterEnabled.observe(this, Observer { buttonUiModel.enabled.value = it })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideLoginAndPassword()
        vm.appTitle.value = getAppTitle()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickEnter()
            R.id.b_topbar_1 -> vm.onClickAuxiliaryMenu()
            R.id.b_topbar_2 -> vm.onClickExit()
        }
    }

    private fun hideLoginAndPassword() {
        binding?.layoutLogin?.apply {
            tvLogin.setInvisible()
            etLogin.setInvisible()
            tvPassword.setInvisible()
            etPassword.setInvisible()
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }
}