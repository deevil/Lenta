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
    lateinit var manager: ITaskManager


    val good by lazy {
        manager.currentGood
    }

    val raw by lazy {
        manager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val defects by lazy {
        good.map { good ->
            val defectPacks = good?.packs?.filter { it.isDefect() }
            defectPacks?.mapIndexed { index, pack ->
                DefectListUi(
                        position = "${defectPacks.size - index}",
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
