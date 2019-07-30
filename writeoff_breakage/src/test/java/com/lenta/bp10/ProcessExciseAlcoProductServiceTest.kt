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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class ProcessExciseAlcoProductServiceTest {
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
    fun testProcessExciseAlcoProduct() {
        var test = false
        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val product3 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        creatingObjectsForTest()

        if (task.processExciseAlcoProduct(product1) == null) {
            test = true
        }
        assertTrue(test)

        test = false
        if (task.processExciseAlcoProduct(product2) == null) {
            test = true
        }
        assertTrue(test)

        test = false
        if (task.processExciseAlcoProduct(product3) != null) {
            test = true
        }
        assertTrue(test)

    }

    @Test
    fun testAddProduct() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        //дублирует продукт2, который не должен добавляться
        val product3 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val reason1 = WriteOffReason("01", "Срок годности", "A")
        val reason2 = WriteOffReason("02", "Срок негодности", "A")

        val exciseStamp1 = TaskExciseStamp("materialNumber1", "1", "материал набора", "Срок годности", false)
        val exciseStamp2 = TaskExciseStamp("materialNumber2", "2", "материал набора", "Срок негодности", false)
        //дублирующая марка, которая не должна добавляться
        val exciseStamp3 = TaskExciseStamp("materialNumber2", "2", "материал набора", "Срок негодности", false)

        task = task.processExciseAlcoProduct(product1)!!
                .add(reason1, 1.0, exciseStamp1)
                .apply()

        assertEquals(1, task.getProcessedProducts().size.toLong())
        assertEquals(1.0, task.getTotalCountOfProduct(product1), 0.0)

        assertEquals(1, task.taskRepository.getExciseStamps().lenght())

        //марка exciseStamp3 не должна добавляться
        task = task.processExciseAlcoProduct(product2)!!
                .add(reason1, 1.0, exciseStamp2)
                .add(reason2, 2.0, exciseStamp3)
                .apply()

        assertEquals(2, task.getProcessedProducts().size.toLong())
        assertEquals(1.0, task.getTotalCountOfProduct(product2), 0.0)

        assertEquals(2, task.taskRepository.getExciseStamps().lenght())

        //TODO (Borisenko) Нужно обсудить необходимость поведения, указанного в комментарии
        ////продукт product3 и марка exciseStamp1 не должены добавляться, а TotalCount (итого списано) у продукта product2 должен быть увеличен до 6
        task = task.processExciseAlcoProduct(product3)!!
                .add(reason1, 2.0, exciseStamp1)
                .apply()

        assertEquals(2, task.getProcessedProducts().size.toLong())
        assertEquals(1.0, task.getTotalCountOfProduct(product2), 0.0)
    }

    @Test
    fun testDelProduct() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val product2 = ProductInfo("materialNumber2", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val product3 = ProductInfo("materialNumber3", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val reason1 = WriteOffReason("01", "Срок годности", "A")
        val reason2 = WriteOffReason("02", "Срок негодности", "A")

        val exciseStamp1 = TaskExciseStamp("materialNumber1", "1", "материал набора", "Срок годности", false)
        val exciseStamp2 = TaskExciseStamp("materialNumber2", "2", "материал набора", "Срок негодности", false)
        val exciseStamp3 = TaskExciseStamp("materialNumber2", "3", "материал набора", "Срок негодности", false)


        task = task.processExciseAlcoProduct(product1)!!
                .add(reason1, 1.0, exciseStamp1)
                .apply()

        task = task.processExciseAlcoProduct(product2)!!
                .add(reason1, 1.0, exciseStamp2)
                .add(reason2, 2.0, exciseStamp3)
                .apply()

        task = task.processExciseAlcoProduct(product3)!!
                .add(reason1, 3.0, exciseStamp3)
                .apply()

        val arrDelProduct = ArrayList<ProductInfo>()
        arrDelProduct.add(product1)
        arrDelProduct.add(product3)

        task = task.deleteProducts(arrDelProduct)

        assertEquals(1, task.getProcessedProducts().size.toLong())
        assertEquals(0.0, task.getTotalCountOfProduct(product1), 0.0)
        assertEquals(3.0, task.getTotalCountOfProduct(product2), 0.0)
        assertEquals(0.0, task.getTotalCountOfProduct(product3), 0.0)



        assertEquals(2, task.taskRepository.getExciseStamps().lenght())
    }

    @Test
    fun testDelReason() {
        creatingObjectsForTest()

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType")

        val reason1 = WriteOffReason("01", "Срок годности", "A")
        val reason2 = WriteOffReason("02", "Срок негодности", "A")

        val exciseStamp1 = TaskExciseStamp("materialNumber1", "1", "материал набора", "Срок годности", false)
        val exciseStamp2 = TaskExciseStamp("materialNumber1", "2", "материал набора", "Срок негодности", false)


        task = task.processExciseAlcoProduct(product1)!!
                .add(reason1, 1.0, exciseStamp1)
                .add(reason2, 1.0, exciseStamp2)
                .apply()

        assertEquals(1, task.getProcessedProducts().size.toLong())
        assertEquals(1.0, task.getTotalCountOfProduct(product1), 1.0)

        assertEquals(2, task.taskRepository.getExciseStamps().lenght())

        taskWriteOfReasonRepository.get(0).let {

            assertEquals(reason1.code, it.writeOffReason.code)

            task.deleteTaskWriteOffReason(it)

            assertEquals(1, task.getProcessedProducts().size.toLong())
            assertEquals(1.0, task.getTotalCountOfProduct(product1), 1.0)
            assertEquals(1, task.taskRepository.getExciseStamps().lenght())

        }

        taskWriteOfReasonRepository.get(0).let {

            assertEquals(reason2.code, it.writeOffReason.code)

            task.deleteTaskWriteOffReason(it)

            assertEquals(0, task.getProcessedProducts().size.toLong())
            assertEquals(0.0, task.getTotalCountOfProduct(product1), 1.0)
            assertEquals(0, task.taskRepository.getExciseStamps().lenght())

        }

    }

}