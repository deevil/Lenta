package com.lenta.bp16.features.warehouse_selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.consts.Consts
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.IDatabaseRepository
import com.lenta.bp16.request.pojo.WarehouseInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchAsyncTryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WarehouseSelectionViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var warehouseStorage: IWarehousePersistStorage

    @Inject
    lateinit var database: IDatabaseRepository

    // список складов
    val warehouseList: MutableLiveData<List<WarehouseInfo>> = MutableLiveData()

    init {
        /*
        There is no data from FM model
        viewModelScope.launch {
            sessionInfo.market?.let {
                val currentWarehouses = database.getWarehouses(it)
                warehouseList.postValue(currentWarehouses)
            }
        }*/
    }

    private val wareHousesSet by unsafeLazy {
        warehouseStorage.getSelectedWarehouses()
    }

    val warehouseOneSelected by unsafeLazy {
        MutableLiveData<Boolean>(wareHousesSet.contains(Consts.WAREHOUSE_0111))
    }

    val warehouseTwoSelected by unsafeLazy {
        MutableLiveData<Boolean>(wareHousesSet.contains(Consts.WAREHOUSE_0121))
    }

    val warehouseThreeSelected by unsafeLazy {
        MutableLiveData<Boolean>(wareHousesSet.contains(Consts.WAREHOUSE_0131))
    }

    val warehouseFourSelected by unsafeLazy {
        MutableLiveData<Boolean>(wareHousesSet.contains(Consts.WAREHOUSE_0141))
    }

    private val warehouseAllSelected by unsafeLazy {
        warehouseOneSelected.map { whOneSelected ->
            warehouseTwoSelected.map { whTwoSelected ->
                warehouseThreeSelected.map { whThreeSelected ->
                    warehouseFourSelected.map { whFourSelected ->
                        whOneSelected == true && whTwoSelected == true && whThreeSelected == true && whFourSelected == true
                    }
                }
            }
        }
    }

    val isAllSelected by unsafeLazy {
        warehouseAllSelected.switchMap {
            liveData { emit(it) }
        }
    }

    fun onAllSelected(isChecked: Boolean) {
        warehouseOneSelected.value = isChecked
        warehouseTwoSelected.value = isChecked
        warehouseThreeSelected.value = isChecked
        warehouseFourSelected.value = isChecked
    }

    fun onClickNext() = launchAsyncTryCatch {
        val selectedWarehousesSet = mutableSetOf<String>()
        if(warehouseOneSelected.value == true) selectedWarehousesSet.add(Consts.WAREHOUSE_0111)
        if(warehouseTwoSelected.value == true) selectedWarehousesSet.add(Consts.WAREHOUSE_0121)
        if(warehouseThreeSelected.value == true) selectedWarehousesSet.add(Consts.WAREHOUSE_0131)
        if(warehouseFourSelected.value == true) selectedWarehousesSet.add(Consts.WAREHOUSE_0141)
        warehouseStorage.saveSelectedWarehouses(selectedWarehousesSet)

        withContext(Dispatchers.Main) {
            navigator.openIngredientsListScreen()
        }
    }
}