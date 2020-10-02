package com.lenta.shared.features.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.scan.OnScanResultListener

abstract class CoreLoginFragment : CoreFragment<com.lenta.shared.databinding.FragmentLoginBinding, CoreAuthViewModel>(),
        OnBackPresserListener, ToolbarButtonsClickListener, OnScanResultListener {

    private var viewFocus: View? = null

    override fun getLayoutId(): Int = R.layout.fragment_login


    override fun onBackPressed(): Boolean {
        return false
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
            buttonUiModel.requestFocus()
            vm.enterEnabled.observe(this, { buttonUiModel.enabled.value = it })
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data, viewFocus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.layoutLogin?.etLogin?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }

        binding?.layoutLogin?.etPassword?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }
    }

}