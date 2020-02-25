package com.lenta.bp12.features.save_data

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class SaveDataViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val title by lazy {
        MutableLiveData("TK - ${sessionInfo.market}")
    }

    val tasks by lazy {
        MutableLiveData(List(3) {
            ItemTaskUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    description = "Test description ${it + 1}"
            )
        })
    }

    // -----------------------------

    fun onClickNext() {

    }


}

data class ItemTaskUi(
        val position: String,
        val name: String,
        val description: String
)