package com.lenta.shared.utilities.databinding

import com.lenta.shared.platform.navigation.ICoreNavigator
import javax.inject.Inject

val dataBindingHelpHolder = DataBindingExtHolder()

class DataBindingExtHolder {
    @Inject
    lateinit var coreNavigator: ICoreNavigator
}