package com.lenta.bp14.features.work_list.storage_z_parts

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.lenta.bp14.features.base.BaseGoodInfoViewModel
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject

class StorageZPartsViewModel : BaseGoodInfoViewModel() {

    @Inject
    lateinit var task: WorkListTask

    var storage: String = ""

    val good by lazy { task.currentGood }

    val title: String by unsafeLazy {
        resourceFormatter.getStorageZPartInfo(storage)
    }

    override val zParts: LiveData<List<ZPartUi>> by unsafeLazy {
        good.switchMap { good ->
            asyncLiveData<List<ZPartUi>> {
                val result = good?.additional?.zParts?.filter { it.stock == storage }
                        .mapToZPartUiList(good.units.name)
                emit(result)
            }
        }
    }
}