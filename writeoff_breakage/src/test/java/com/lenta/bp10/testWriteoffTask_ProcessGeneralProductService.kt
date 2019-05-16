package com.lenta.bp10

import com.lenta.bp10.models.memory.MemoryTaskRepository
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.TaskType
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.models.task.WriteOffTask
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class testWriteoffTask_ProcessGeneralProductService {
    lateinit var taskDescription: TaskDescription
    var taskRepository: ITaskRepository = MemoryTaskRepository()
    lateinit var task: WriteOffTask

    fun creatingObjectsForTest() {
        taskDescription = TaskDescription(
                TaskType("СГП", "nСГП"),
                "Списание от 04.06 10:23",
                "0002",
                listOf(WriteOffReason("949ВД", "Лом/Бой")),
                listOf("N"),
                listOf("2FER", "3ROH"), "perNo", "printer", "tkNumber", "ipAddress"
        )

        task = WriteOffTask(taskDescription, taskRepository)
    }

    @Test
    fun testProcessGeneralProduct() {
        var test = false
        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, 1, MatrixType.Active, "materialType")

        val product3 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType")

        creatingObjectsForTest()

        if (task.processGeneralProduct(product1) != null) {
            test = true
        }
        assertTrue(test)

        test = false
        if (task.processGeneralProduct(product2) == null) {
            test = true
        }
        assertTrue(test)

        test = false
        if (task.processGeneralProduct(product3) == null) {
            test = true
        }
        assertTrue(test)

    }

    @Test
    fun testAddProduct() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")

        //дублирует продукт2, который не должен добавляться
        val product3 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")


        val reason1 = WriteOffReason("01", "Срок годности")
        val reason2 = WriteOffReason("02", "Срок негодности")

        task = task.processGeneralProduct(product1)!!
                .add(reason1, 1.0)
                .apply()

        assertEquals(1, task.getProcessedProducts().size.toLong())
        assertEquals(1.0, task.getTotalCountOfProduct(product1), 0.0)

        task = task.processGeneralProduct(product2)!!
                .add(reason1, 1.0)
                .add(reason2, 2.0)
                .apply()

        assertEquals(2, task.getProcessedProducts().size.toLong())
        assertEquals(3.0, task.getTotalCountOfProduct(product2), 0.0)

        //продукт product3 не должен добавляться, а TotalCount (итого списано) у продукта product2 должен быть увеличен до 5
        task = task.processGeneralProduct(product3)!!
                .add(reason1, 2.0)
                .apply()

        assertEquals(2, task.getProcessedProducts().size.toLong())
        assertEquals(5.0, task.getTotalCountOfProduct(product2), 0.0)
    }

    @Test
    fun testDelProduct() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")

        val product3 = ProductInfo("materialNumber3", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")


        val reason1 = WriteOffReason("01", "Срок годности")
        val reason2 = WriteOffReason("02", "Срок негодности")

        task = task.processGeneralProduct(product1)!!
                .add(reason1, 1.0)
                .apply()

        task = task.processGeneralProduct(product2)!!
                .add(reason1, 1.0)
                .add(reason2, 2.0)
                .apply()

        task = task.processGeneralProduct(product3)!!
                .add(reason1, 3.0)
                .apply()

        val arrDelProduct = ArrayList<ProductInfo>()
        arrDelProduct.add(product1)
        arrDelProduct.add(product3)

        task = task.deleteProducts(arrDelProduct)

        assertEquals(1, task.getProcessedProducts().size.toLong())
        assertEquals(0.0, task.getTotalCountOfProduct(product1), 0.0)
        assertEquals(3.0, task.getTotalCountOfProduct(product2), 0.0)
        assertEquals(0.0, task.getTotalCountOfProduct(product3), 0.0)
    }

    @Test
    fun testClearTask() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType")


        val reason1 = WriteOffReason("01", "Срок годности")
        val reason2 = WriteOffReason("02", "Срок негодности")

        task = task.processGeneralProduct(product1)!!
                .add(reason1, 1.0)
                .apply()

        task = task.processGeneralProduct(product2)!!
                .add(reason1, 1.0)
                .add(reason2, 2.0)
                .apply()

        task.clearTask()

        assertEquals(0, task.getProcessedProducts().size.toLong())
        assertEquals(0.0, task.getTotalCountOfProduct(product1), 0.0)
        assertEquals(0.0, task.getTotalCountOfProduct(product2), 0.0)
    }
}