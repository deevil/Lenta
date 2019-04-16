package com.lenta.bp10.features.auth

import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.activity.main.MainActivity
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.CoreLoginFragment
import com.lenta.shared.utilities.extentions.implementationOf

class AuthFragment : CoreLoginFragment() {
    override fun getViewModel(): CoreAuthViewModel {
        ViewModelProviders.of(this).get(AuthViewModel::class.java).let {
            activity.implementationOf(MainActivity::class.java)?.appComponent?.inject(it)
            return it
        }
    }

}