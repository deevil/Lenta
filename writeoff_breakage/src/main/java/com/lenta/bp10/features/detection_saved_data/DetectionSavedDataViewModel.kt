package com.lenta.bp10.features.detection_saved_data

import com.lenta.bp10.models.IPersistWriteOffTask
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.message.MessageViewModel
import javax.inject.Inject

class DetectionSavedDataViewModel : MessageViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var writeOffTaskManager: IWriteOffTaskManager
    @Inject
    lateinit var persistWriteOffTask: IPersistWriteOffTask

    fun onClickDelete() {
        writeOffTaskManager.clearTask()
        persistWriteOffTask.saveWriteOffTask(null)
        screenNavigator.openMainMenuScreen()

    }

    override fun onClickApply() {
        writeOffTaskManager.setTask(persistWriteOffTask.getSavedWriteOffTask())
        screenNavigator.goBack()
        screenNavigator.openJobCardScreen()
    }
}
