package com.lenta.bp14.features.work_list.storage_z_parts

import androidx.lifecycle.LiveData
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.resource.IResourceFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject

class StorageZPartsViewModel : CoreViewModel() {
    @Inject
    lateinit var task: WorkListTask

    @Inject
    lateinit var resourceFormatter: IResourceFormatter

    val good by lazy { task.currentGood }
    lateinit var storage: String

    val title: String
        get() = resourceFormatter.getStorageZPartInfo(storage)

    val zParts: LiveData<List<ZPartUi>> by unsafeLazy {
        good.map { good ->
            good?.additional?.zParts?.filter { it.stock == storage }?.mapIndexed { index, zPart ->
                val quantity = "${zPart.quantity.dropZeros()} ${good.units.name}"
                ZPartUi(
                        "${index + 1}",
                        zPart.stock,
                        resourceFormatter.getFormattedZPartInfo(zPart),
                        quantity
                )
            }
        }
    }
}