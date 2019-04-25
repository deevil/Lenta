package com.lenta.bp10

import com.lenta.bp10.models.memory.MemoryTaskExciseStampRepository
import com.lenta.bp10.models.memory.MemoryTaskProductRepository
import com.lenta.bp10.models.memory.MemoryTaskRepository
import com.lenta.bp10.models.memory.MemoryTaskWriteOffReasonRepository
import com.lenta.bp10.models.repositories.ITaskExciseStampRepository
import com.lenta.bp10.models.repositories.ITaskProductRepository
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.ITaskWriteOffReasonRepository
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.TaskType
import com.lenta.bp10.models.task.WriteOffTask
import com.lenta.bp10.rest.dataModels.SaveTaskDataToSapRestRequest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class testWriteoffTask_TaskSaveModel {
    lateinit var taskDescription: TaskDescription
    var taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository()
    var taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository()
    var taskWriteOfReasonRepository: ITaskWriteOffReasonRepository = MemoryTaskWriteOffReasonRepository()
    var taskRepository: ITaskRepository = MemoryTaskRepository(taskProductRepository, taskExciseStampRepository, taskWriteOfReasonRepository)
    lateinit var task: WriteOffTask

    fun creatingObjectsForTest() {
        taskDescription = TaskDescription(
                TaskType("СГП", "nСГП"),
                "Списание от 04.06 10:23",
                "0002",
                ArrayList(Arrays.asList("949ВД")),
                ArrayList(Arrays.asList("N")),
                ArrayList(Arrays.asList("2FER", "3ROH")), "0001", "printer", "tkNumber", "ipAdress"
        )

        task = WriteOffTask(taskDescription, taskRepository)
    }

    @Test
    fun testGetTaskSaveModel() {
        var test = false
        val sapClient = "500"

        creatingObjectsForTest()

        var request1 = SaveTaskDataToSapRestRequest()
        request1 = request1.setSapClient(sapClient)
        if (task.getTaskSaveModel().getPerNo() == "0001") {
            test = true
        }
        assertTrue(test)

        test = false
        if (request1.sapClient == "500") {
            test = true
        }
        assertTrue(test)
    }
}