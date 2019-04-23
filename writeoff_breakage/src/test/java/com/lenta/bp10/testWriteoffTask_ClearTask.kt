package com.lenta.bp10

import com.lenta.bp10.models.memory.MemoryTaskExciseStampRepository
import com.lenta.bp10.models.memory.MemoryTaskProductRepository
import com.lenta.bp10.models.memory.MemoryTaskRepository
import com.lenta.bp10.models.memory.MemoryTaskWriteOffReasonRepository
import com.lenta.bp10.models.repositories.ITaskExciseStampRepository
import com.lenta.bp10.models.repositories.ITaskProductRepository
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.ITaskWriteOffReasonRepository
import com.lenta.bp10.models.task.*
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class testWriteoffTask_ClearTask {
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
                ArrayList(Arrays.asList("2FER", "3ROH")), "perNo", "printer", "tkNumber", "ipAdress"
        )

        task = WriteOffTask(taskDescription, taskRepository)
    }

    @Test
    fun testClearTask() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, 1, MatrixType.Active, "materialType")

        val product3 = ProductInfo("materialNumber3", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType")

        val reason1 = WriteOffReason("01", "Срок годности")
        val reason2 = WriteOffReason("02", "Срок негодности")

        val exciseStamp1 = TaskExciseStamp("materialNumber3", "1", "материал набора", "Срок годности", false)
        val exciseStamp2 = TaskExciseStamp("materialNumber3", "2", "материал набора", "Срок негодности", false)


        task = task.processGeneralProduct(product1)!!
                .add(reason1, 1.0)
                .apply()

        task = task.processNonExciseAlcoProduct(product2)!!
                .add(reason1, 1.0)
                .add(reason2, 2.0)
                .apply()

        task = task.processExciseAlcoProduct(product3)!!
                .add(reason1, 1.0, exciseStamp1)
                .add(reason1, 1.0, exciseStamp2)
                .apply()

        task.clearTask()

        assertEquals(0, task.getProcessedProducts().size.toLong())
        assertEquals(0.0, task.getTotalCountOfProduct(product1), 0.0)
        assertEquals(0.0, task.getTotalCountOfProduct(product2), 0.0)
        assertEquals(0.0, task.getTotalCountOfProduct(product3), 0.0)
    }
}