package com.lenta.bp14.features.not_exposed.storage_z_parts

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.lenta.bp14.features.base.BaseGoodInfoViewModel
import com.lenta.bp14.models.not_exposed.INotExposedTask
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class StorageZPartsNotExposedViewModel : BaseGoodInfoViewModel() {

    @Inject
    lateinit var task: INotExposedTask

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
}