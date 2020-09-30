package com.lenta.bp12.features.enter_mrc

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.WorkType
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class EnterMrcViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: ICoreNavigator

    @Inject
    lateinit var createTaskManager: ICreateTaskManager

    @Inject
    lateinit var openTaskManager: IOpenTaskManager

    @Inject
    lateinit var coreNavigator: ICoreNavigator

    val requestFocus = MutableLiveData(true)

    var workType = WorkType.CREATE

    /**
     * Это поле нужно для того чтобы вернуть callback
     * */
    var codeConfirmForRight: Int? = null

    val mrcField: MutableLiveData<String> = MutableLiveData("")

    val task by unsafeLazy {
        chooseManager().currentTask
    }

    val good by unsafeLazy {
        chooseManager().currentGood
    }

    val title by unsafeLazy {
        good.map { good ->
            good?.getNameWithMaterial() ?: task.value?.getFormattedName()
        }
    }

    val enabledNextBtn: MutableLiveData<Boolean> = mrcField.mapSkipNulls {
        it.isNotEmpty()
    }

    fun onClickNext() {
        val manager = when (workType) {
            WorkType.CREATE -> createTaskManager
            WorkType.OPEN -> openTaskManager
        }
        val currentGood = good.value
        currentGood?.maxRetailPrice = mrcField.value.orEmpty()
        manager.updateCurrentGood(currentGood)

        codeConfirmForRight?.let {
            coreNavigator.goBackWithResultCode(it)
        }
    }

    private fun chooseManager(): ITaskManager<*> {
        return when (workType) {
            WorkType.CREATE -> createTaskManager
            WorkType.OPEN -> openTaskManager
        }
    }

}
