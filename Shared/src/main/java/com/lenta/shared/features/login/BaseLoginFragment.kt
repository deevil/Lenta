package com.lenta.shared.features.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.BaseFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo

abstract class BaseLoginFragment : BaseFragment<com.lenta.shared.databinding.FragmentLoginBinding, BaseAuthViewModel>(), OnBackPresserListener, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_login


    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBottomToolBarUIModel()?.let { bottomToolbarUiModel ->
            bottomToolbarUiModel.cleanAll()
            bottomToolbarUiModel.uiModelButton5.let { buttonUiModel ->
                buttonUiModel.show(ButtonDecorationInfo.enterToApp)
                vm.enterEnabled.observe(this, Observer { buttonUiModel.enabled.value = it })
            }


        }

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickEnter()
        }
    }


}