package com.lenta.shared.features.alert


import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class AlertFragment : CoreMessageFragment() {

    var pageNumb by state("")


    override fun getPageNumber(): String = pageNumb


    override fun getViewModel(): MessageViewModel {
        provideViewModel(MessageViewModel::class.java).let { vm ->
            coreComponent.inject(vm)
            vm.message = message
            vm.iconRes = iconRes
            vm.codeConfirm = codeConfirm

            return vm
        }
    }


    companion object {
        fun create(
                message: String,
                iconRes: Int = 0,
                pageNumber: String = "???",
                codeConfirm: Int? = null): AlertFragment {
            AlertFragment().let {
                it.message = message
                it.iconRes = iconRes
                it.codeConfirm = codeConfirm
                it.pageNumb = pageNumber
                return it
            }
        }
    }

}