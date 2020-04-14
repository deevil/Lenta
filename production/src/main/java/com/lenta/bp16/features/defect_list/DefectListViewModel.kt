package com.lenta.bp16.features.defect_list

import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class DefectListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager


    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val defects by lazy {
        good.map { good ->
            good?.packs?.filter { it.isDefect() }?.mapIndexed { index, pack ->
                DefectListUi(
                        position = "${index + 1}",
                        packAndCategory = "${pack.code} / ${pack.category?.description}",
                        cause = pack.defect?.description ?: "",
                        weight = "${pack.quantity.dropZeros()} ${good.units.name}"
                )
            }
        }
    }

}

data class DefectListUi(
        val position: String,
        val packAndCategory: String,
        val cause: String,
        val weight: String
)
