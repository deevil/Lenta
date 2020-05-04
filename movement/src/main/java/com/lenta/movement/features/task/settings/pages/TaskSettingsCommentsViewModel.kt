package com.lenta.movement.features.task.settings.pages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lenta.movement.models.ITaskManager
import javax.inject.Inject

class TaskSettingsCommentsViewModel: ViewModel() {

    @Inject
    lateinit var taskManager: ITaskManager

    val description by lazy { MutableLiveData(taskManager.getTaskAnnotation()) }
    val comments by lazy { MutableLiveData(taskManager.getTask().description) }

}