package com.lenta.shared.features.login

import android.os.Bundle
import android.view.View
import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.BaseFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.Logg

class LoginFragment : BaseFragment<com.lenta.shared.databinding.FragmentLoginBinding>(), OnBackPresserListener {


    override fun getLayoutId(): Int = R.layout.fragment_login

    override fun onBackPressed(): Boolean {
        Logg.d { "onBackPressed" }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBottomToolBarUIModel()?.let {
            it.cleanAll()
            it.visibility.value = true
            it.uiModelButton5.show(ButtonDecorationInfo.enterToApp)

        }
    }

}