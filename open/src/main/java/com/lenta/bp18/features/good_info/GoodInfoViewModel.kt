package com.lenta.bp18.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.model.pojo.GoodInfo
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo

    val deviceIp = MutableLiveData("")

    val good: MutableLiveData<Good> = MutableLiveData()

    init {

    }

    protected fun setGoodByEan(ean: String){
        Logg.d { "Entered EAN: $ean" }
        viewModelScope.launch {
            val goodInfo = database.getGoodInfoByEan(ean)
            if(goodInfo != null){

            }
        }
    }

    fun getSelectGood(barcode: String){

    }


    fun onClickComplete(){
        viewModelScope.launch {
            navigator.showProgressLoadingData()

        }
    }
}