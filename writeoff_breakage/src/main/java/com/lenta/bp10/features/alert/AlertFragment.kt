package com.lenta.bp10.features.alert

import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.activity.main.MainActivity
import com.lenta.shared.features.message.BaseMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.utilities.extentions.implementationOf

class AlertFragment : BaseMessageFragment() {

    override fun getViewModel(): MessageViewModel {
        ViewModelProviders.of(this).get(MessageViewModel::class.java).let { vm ->
            activity.implementationOf(MainActivity::class.java)?.appComponent?.inject(vm)
            message?.let {
                vm.message = it
            }

            return vm
        }
    }

    companion object {
        fun create(message: String): AlertFragment {
            AlertFragment().let {
                it.message = message
                return it
            }
        }
    }

}