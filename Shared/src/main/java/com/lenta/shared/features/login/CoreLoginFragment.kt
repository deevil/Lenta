package com.lenta.shared.features.login

import android.view.View
import androidx.lifecycle.Observer
import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener

abstract class CoreLoginFragment : CoreFragment<com.lenta.shared.databinding.FragmentLoginBinding, CoreAuthViewModel>(), OnBackPresserListener, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_login


    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickEnter()
            R.id.b_topbar_1 -> vm.onClickAuxiliaryMenu()
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton5.let { buttonUiModel ->
            buttonUiModel.show(ButtonDecorationInfo.enterToApp)
            vm.enterEnabled.observe(this, Observer { buttonUiModel.enabled.value = it })
        }
    }


}