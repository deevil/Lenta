package com.lenta.bp10.models

import com.google.gson.Gson
import com.lenta.bp10.models.memory.MemoryTaskExciseStampRepository
import com.lenta.bp10.models.memory.MemoryTaskProductRepository
import com.lenta.bp10.models.memory.MemoryTaskRepository
import com.lenta.bp10.models.memory.MemoryTaskWriteOffReasonRepository
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.bp10.models.task.WriteOffTask
import com.lenta.shared.models.core.ProductInfo
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PersistWriteOffTask @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IPersistWriteOffTask {

    private val keyForSave = "WRITE_OFF_TASK_VALUE"

    override fun saveWriteOffTask(writeOffTask: WriteOffTask?) {
        if (writeOffTask == null) {
            hyperHive.stateAPI.saveParamToDB(keyForSave, null)
            return
        }
        PersistWriteOffTaskData(
                taskDescription = writeOffTask.taskDescription,
                products = writeOffTask.taskRepository.getProducts().getProducts(),
                writeOffReasons = writeOffTask.taskRepository.getWriteOffReasons().getWriteOffReasons(),
                exciseStamps = writeOffTask.taskRepository.getExciseStamps().getExciseStamps()
        ).let {
            hyperHive.stateAPI.saveParamToDB(keyForSave, gson.toJson(it))
        }
    }

    override fun getSavedWriteOffTask(): WriteOffTask? {
        hyperHive.stateAPI.getParamFromDB(keyForSave)?.let { json ->
            gson.fromJson(json, PersistWriteOffTaskData::class.java)?.let { persistWriteOffTaskData ->
                return WriteOffTask(
                        taskDescription = persistWriteOffTaskData.taskDescription,
                        taskRepository = MemoryTaskRepository(
                                taskProductRepository = MemoryTaskProductRepository(ArrayList(persistWriteOffTaskData.products)),
                                taskExciseStampRepository = MemoryTaskExciseStampRepository(ArrayList(persistWriteOffTaskData.exciseStamps)),
                                taskWriteOfReasonRepository = MemoryTaskWriteOffReasonRepository(ArrayList(persistWriteOffTaskData.writeOffReasons))
                        ))
            }
        }
        return null
    }

}

interface IPersistWriteOffTask {
    fun saveWriteOffTask(writeOffTask: WriteOffTask?)
    fun getSavedWriteOffTask(): WriteOffTask?
}

data class PersistWriteOffTaskData(
        val taskDescription: TaskDescription,
        val products: List<ProductInfo>,
        val writeOffReasons: List<TaskWriteOffReason>,
        val exciseStamps: List<TaskExciseStamp>
)