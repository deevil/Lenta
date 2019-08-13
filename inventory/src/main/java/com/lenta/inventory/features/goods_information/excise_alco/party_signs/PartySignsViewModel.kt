package com.lenta.inventory.features.goods_information.excise_alco.party_signs

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class PartySignsViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: ICoreNavigator

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val manufacturersName: MutableLiveData<List<String>> = MutableLiveData()

    val stampLength: MutableLiveData<Int> = MutableLiveData()

    val bottlingDate: MutableLiveData<String> = MutableLiveData("")

    val dateNotSpecified: MutableLiveData<String> = MutableLiveData()

    fun onClickNext(){
        if (bottlingDate.value.isNullOrEmpty()) {
            screenNavigator.openInfoScreen(dateNotSpecified.value!!)
        } else {
            screenNavigator.goBackWithArgs(Bundle().apply {
                putInt("stampLength", stampLength.value!!)
                putString("manufacturerCode", selectedPosition.value.toString())
                putString("bottlingDate", bottlingDate.value)
            })
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}
