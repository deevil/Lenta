package com.lenta.bp14.features.not_exposed.storage_z_parts

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.lenta.bp14.features.base.BaseGoodInfoViewModel
import com.lenta.bp14.models.not_exposed.INotExposedTask
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class StorageZPartsNotExposedViewModel : BaseGoodInfoViewModel() {

    @Inject
    lateinit var task: INotExposedTask

    @Inject
    lateinit var screenNavigator: IScreenNavigator


    lateinit var storage: String

    val goodInfo by lazy {
        task.getProcessedProductInfoResult()!!.goodInfo
    }

    val title: String by unsafeLazy {
        resourceFormatter.getStorageZPartInfo(storage)
    }

    val zParts: LiveData<List<ZPartUi>> by unsafeLazy {
        asyncLiveData<List<ZPartUi>> {
            val result = goodInfo.zParts.filter { it.stock == storage }
                    .mapToZPartUiList(goodInfo.units?.name.orEmpty())
            emit(result)
        }
    }

    fun showZPartInfo(index: Int) {
        zParts.value?.getOrNull(index)?.let { zPart ->
            screenNavigator.openZPartInfoFragment(zPart)
        }.orIfNull {
            Logg.w { "ZPart value is null!" }
            screenNavigator.showAlertWithStockItemNotFound()
        }
    }
}