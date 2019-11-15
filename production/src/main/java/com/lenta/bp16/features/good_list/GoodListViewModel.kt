package com.lenta.bp16.features.good_list

import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class GoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "ЕО - 31354361354313546543131"
    }

}
