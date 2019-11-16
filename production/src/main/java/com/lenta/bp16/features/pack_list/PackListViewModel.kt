package com.lenta.bp16.features.pack_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Pack
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class PackListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "000021 - Форель заморож."
    }

    val packs = MutableLiveData<List<Pack>>(emptyList())

}

data class ItemPackListUi(
        val position: String,
        val packNumber: String,
        val rawName: String,
        val totalWeight: String
)