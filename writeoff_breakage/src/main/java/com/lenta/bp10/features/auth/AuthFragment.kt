package com.lenta.bp10.features.auth

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.CoreLoginFragment
import com.lenta.shared.utilities.extentions.provideViewModel

class AuthFragment : CoreLoginFragment() {
    override fun getViewModel(): CoreAuthViewModel {
        provideViewModel(AuthViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

}