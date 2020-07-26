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
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.models.task.WriteOffTask
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class ProcessNonExciseAlcoProductServiceTest {
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
                ArrayList(Arrays.asList(WriteOffReason("949ВД", "Лом/Бой", "A"))),
                ArrayList(Arrays.asList("N")),
                ArrayList(Arrays.asList("2FER", "3ROH")), "perNo", "printer", "tkNumber", "ipAddress"
        )

        task = WriteOffTask(taskDescription, taskRepository)
    }


    @Test
    fun testAddProduct() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        //дублирует продукт2, который не должен добавляться
        val product3 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val reason1 = WriteOffReason("01", "Срок годности", "A")
        val reason2 = WriteOffReason("02", "Срок негодности", "A")

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

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val product3 = ProductInfo("materialNumber3", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")


        val reason1 = WriteOffReason("01", "Срок годности", "A")
        val reason2 = WriteOffReason("02", "Срок негодности", "A")

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
}