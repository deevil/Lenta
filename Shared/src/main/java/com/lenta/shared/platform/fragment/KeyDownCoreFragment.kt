package com.lenta.shared.platform.fragment

import androidx.databinding.ViewDataBinding
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg

abstract class KeyDownCoreFragment<T, S> : CoreFragment<T, S>(),
        OnKeyDownListener where T : ViewDataBinding, S : CoreViewModel {

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        return getCurrentKeyHandler()?.onKeyDown(keyCode) ?: false
    }

}