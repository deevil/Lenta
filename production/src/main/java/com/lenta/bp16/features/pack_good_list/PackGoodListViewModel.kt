package com.lenta.bp16.features.pack_good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Good
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class PackGoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "ЕО - 236589267462146198"
    }

    val goods = MutableLiveData<List<Good>>(emptyList())

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        // Переход к карточке товара

    }

}

data class ItemPackGoodListUi(
        val position: String,
        val name: String,
        val planWeight: String
)