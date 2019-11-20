package com.lenta.bp16.features.raw_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Raw
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class RawListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "000021 - Форель заморож."
    }

    val puNumber by lazy {
        "31354361354313546543131"
    }

    val raw = MutableLiveData<List<Raw>>(emptyList())

    val completeEnabled = MutableLiveData(true)

    // -----------------------------

    fun onClickComplete() {
        // Непонятно, что делает кнопка...

    }

    fun onClickItemPosition(position: Int) {
        // Переход к карточке товара для взвешивания

    }

}

data class ItemRawListUi(
        val position: String,
        val name: String,
        val processed: String
)