package com.lenta.bp7.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckStoreData
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkStoreData: CheckStoreData

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelfNumber: MutableLiveData<String> = MutableLiveData()

    val goods: MutableLiveData<List<Good>> = MutableLiveData()

    init {
        viewModelScope.launch {
            checkStoreData.let {
                segmentNumber.value = it.currentSegment.number
                shelfNumber.value = it.currentShelf.number
                goods.value = it.currentShelf.goods
            }
        }
    }

    fun onClickApply() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
