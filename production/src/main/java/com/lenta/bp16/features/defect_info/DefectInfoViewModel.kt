package com.lenta.bp16.features.defect_info

import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class DefectInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager


    val good by lazy {
        taskManager.currentGood
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

}
