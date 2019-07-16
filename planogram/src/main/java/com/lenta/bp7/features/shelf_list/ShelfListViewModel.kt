package com.lenta.bp7.features.shelf_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckStoreData
import com.lenta.bp7.data.model.Shelf
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
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

    val selectionsHelper = SelectionItemsHelper()

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelves: MutableLiveData<List<Shelf>> = MutableLiveData()

    init {
        viewModelScope.launch {
            checkStoreData.currentSegment?.let {
                segmentNumber.value = it.number
                shelves.value = it.shelves
            }
        }
    }

    fun onClickDelete() {
        // todo подтверждение удаления полок/сегмента

    }

    fun onClickApply() {
        // todo экран подтверждение завершения сканирования сегмента

    }

    fun onClickItemPosition(position: Int) {
        checkStoreData.currentShelf = shelves.value!![position]
        navigator.openGoodListScreen()
    }
}
