package com.lenta.bp14.models.check_list

import com.google.gson.Gson
import com.lenta.bp14.di.CheckListComponent
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.models.check_list.repo.CheckListRepo
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.time.ITimeMonitor
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

@AppScope
class CheckListTaskManager @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val hyperHive: HyperHive,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson,
        private val soundPlayer: ISoundPlayer,
        private val vibrateHelper: IVibrateHelper
) : BaseTaskManager<ICheckListTask, CheckListTaskDescription>() {

    override fun newTask(taskDescription: CheckListTaskDescription): ICheckListTask? {
        _task = CheckListTask(
                generalRepo = generalRepo,
                checkListRepo = CheckListRepo(hyperHive),
                taskDescription = taskDescription,
                timeMonitor = timeMonitor,
                gson = gson,
                soundPlayer = soundPlayer,
                vibrateHelper = vibrateHelper
        )
        return _task
    }

    override fun getComponentClass(): Class<out Any> {
        return CheckListComponent::class.java
    }

}

