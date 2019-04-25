package com.lenta.bp10.features.auxiliary_menu

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.auxiliary_menu.CoreAuxiliaryMenuFragment
import com.lenta.shared.features.auxiliary_menu.CoreAuxiliaryMenuViewModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AuxiliaryMenuFragment : CoreAuxiliaryMenuFragment(){
    override fun getPageNumber(): String = "10/50"

    override fun getViewModel(): CoreAuxiliaryMenuViewModel {
        provideViewModel(AuxiliaryMenuViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }
}