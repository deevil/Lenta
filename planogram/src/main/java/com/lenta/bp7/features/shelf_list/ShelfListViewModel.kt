package com.lenta.bp7.features.shelf_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckStoreData
import com.lenta.bp7.data.model.Shelf
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShelfListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkStoreData: CheckStoreData

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelves: MutableLiveData<List<Shelf>> = MutableLiveData()

    init {
        viewModelScope.launch {
            checkStoreData.let {
                segmentNumber.value = it.currentSegment.number
                val numberOfShelves = it.currentSegment.shelves.size
                Logg.d { "Count of shelves: $numberOfShelves" }
                shelves.value = it.currentSegment.shelves
            }
        }
    }

    fun onClickDelete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onClickApply() {
        // Заглушка
        navigator.openGoodListScreen()
    }
}
