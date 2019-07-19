package com.lenta.bp7.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData

    val good: MutableLiveData<Good> = MutableLiveData()

    val facings: MutableLiveData<String> = MutableLiveData()

    val missingButtonEnabled: MutableLiveData<Boolean> = facings.map { if (it?.isNotEmpty() == true) it.toInt() == 0 else true }
    val applyButtonEnabled: MutableLiveData<Boolean> = facings.map { if (it?.isNotEmpty() == true) it.toInt() > 0 else false }

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentSegment().getCurrentShelf().getCurrentGood()
        }
    }

    fun onClickMissing() {

    }

    fun onClickApply() {

    }

    fun onClickBack() {

    }

}
