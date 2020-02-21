package com.lenta.bp12.features.save_data

import com.lenta.shared.platform.viewmodel.CoreViewModel

class SaveDataViewModel : CoreViewModel() {

    // TODO: Implement the ViewModel
}

data class ItemTaskUi(
        val position: String,
        val name: String,
        val description: String
)