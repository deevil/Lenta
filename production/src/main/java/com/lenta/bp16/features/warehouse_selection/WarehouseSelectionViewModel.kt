package com.lenta.bp16.features.warehouse_selection

import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class WarehouseSelectionViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
}