package com.lenta.bp16.features.pack_list

import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class PackListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "000021 - Форель заморож."
    }

}
