package com.lenta.inventory

import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.memory.MemoryTaskExciseStampRepository
import com.lenta.inventory.models.memory.MemoryTaskProductRepository
import com.lenta.inventory.models.memory.MemoryTaskRepository
import com.lenta.inventory.models.memory.MemoryTaskStorePlaceRepository
import com.lenta.inventory.models.task.*
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import org.junit.Assert
import org.junit.Test

class testInventoryTask_ProcessGeneralProductService {

    val taskRepository = MemoryTaskRepository()
    val taskProductRepository = MemoryTaskProductRepository()
    val taskExciseStampRepository = MemoryTaskExciseStampRepository()
    val taskStorePlaceRepository = MemoryTaskStorePlaceRepository()

    lateinit var taskDescription: TaskDescription
    lateinit var inventoryTask: InventoryTask
    lateinit var storePlaceProcessing: StorePlaceProcessing

    fun creatingObjectsForTest() {
        taskDescription = TaskDescription(
                taskNumber = "4485",
                taskName = "12 неделя",
                taskType = "ВИ",
                stock = "0001",
                isRecount = true,
                isStrict = true,
                blockType = "",
                lockUser = "",
                lockIP = "",
                productsInTask = 6,
                isStarted = true,
                dateFrom = "2019-03-18",
                dateTo = "2019-07-24",
                taskDeadLine = "2019-07-25",
                recountType = RecountType.None,
                gis = GisControl.GeneralProduct
        )

        inventoryTask = InventoryTask(taskDescription, taskRepository)
        storePlaceProcessing = StorePlaceProcessing(inventoryTask, storePlaceNumber = "123456789")
    }



    @Test
    fun testIsGeneralProduct() {

        creatingObjectsForTest()

        var test = false

        val product1 = TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", null, false)

        val product2 = TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","2", null, false)

        val product3 = TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType", "3", null, false)

        if (inventoryTask.processGeneralProduct(product1) != null) {
            test = true
        }
        Assert.assertTrue(test)

        test = false
        if (inventoryTask.processGeneralProduct(product2) != null) {
            test = true
        }
        Assert.assertTrue(test)

        test = false
        if (inventoryTask.processGeneralProduct(product3) == null) {
            test = true
        }
        Assert.assertTrue(test)
    }

    /**@Test
    fun testAddProductsInRepository() {

        creatingObjectsForTest()

        val product1 = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", null, false)

        val product2 = TaskProductInfo("materialNumber222", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","2", null, false)

        val product3 = TaskProductInfo("materialNumber333", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType", "3", null, false)

        //TODO добавляем продукты из REST-96 в репозиторий
        inventoryTask.addProduct(product1)
        inventoryTask.addProduct(product2)
        storePlaceProcessing.inventoryTask.addProduct(product3)
        Assert.assertEquals(3, storePlaceProcessing.inventoryTask.taskRepository.getProducts().getProducts().size.toLong())
        //Assert.assertEquals(1.0, task.getTotalCountOfProduct(product1), 0.0)

    }

    @Test
    fun testGetNotProcessedProducts() {

        creatingObjectsForTest()

        val product1 = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", null, false)

        val product2 = TaskProductInfo("materialNumber222", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","2", null, false)

        val product3 = TaskProductInfo("materialNumber333", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType", "3", 3.0, true)

        //TODO добавляем продукты из REST-96 в репозиторий
        inventoryTask.addProduct(product1)
        inventoryTask.addProduct(product2)
        inventoryTask.addProduct(product3)
        //TODO возвращаем НЕ ОБРАБОТАННЫЕ продукты, должно быть 2
        Assert.assertEquals(2, storePlaceProcessing.inventoryTask.taskRepository.getProducts().getNotProcessedProducts().size.toLong())
    }

    @Test
    fun testGetProcessedProducts() {

        creatingObjectsForTest()

        val product1 = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", null, false)

        val product2 = TaskProductInfo("materialNumber222", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","2", 2.0, true)

        val product3 = TaskProductInfo("materialNumber333", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType", "3", 3.0, true)

        //TODO добавляем продукты из REST-96 в репозиторий
        inventoryTask.addProduct(product1)
        inventoryTask.addProduct(product2)
        inventoryTask.addProduct(product3)
        //TODO возвращаем ОБРАБОТАННЫЕ продукты, должно быть 2
        Assert.assertEquals(2, storePlaceProcessing.inventoryTask.taskRepository.getProducts().getProcessedProducts().size.toLong())
    }

    @Test
    fun testAddExciseStampsInRepository() {

        creatingObjectsForTest()

        val exciseStamp1 = TaskExciseStamp("materialNumber1", "1", "1", "1", "", zprod = null, bottMark = "", isBadStamp = false)
        val exciseStamp2 = TaskExciseStamp("materialNumber2", "2", "2", "2", "", zprod = null, bottMark = "", isBadStamp = false)
        //дублирующая марка, которая не должна добавляться
        val exciseStamp3 = TaskExciseStamp("materialNumber2", "2", "2", "2", "", zprod = null, bottMark = "", isBadStamp = false)

        //TODO добавляем акцизные марки из REST-96 в репозиторий
        inventoryTask.addExciseStamp(exciseStamp1)
        inventoryTask.addExciseStamp(exciseStamp2)
        inventoryTask.addExciseStamp(exciseStamp3)
        //TODO возвращаем кол-во акцизных марок, должно быть 2
        Assert.assertEquals(2, storePlaceProcessing.inventoryTask.taskRepository.getExciseStamps().getExciseStamps().size.toLong())
    }

    @Test
    fun testAddStorePlaceInRepository() {

        creatingObjectsForTest()

        val storePlace1 = TaskStorePlaceInfo("0001", "1", "1", false)
        val storePlace2 = TaskStorePlaceInfo("0002", "2", "2", false)
        //дублирующее место хранения, которое не должно добавляться
        val storePlace3 = TaskStorePlaceInfo("0001", "2", "2", false)

        //TODO добавляем места хранения из REST-92 в репозиторий
        inventoryTask.addStorePlace(storePlace1)
        inventoryTask.addStorePlace(storePlace2)
        inventoryTask.addStorePlace(storePlace3)
        //TODO возвращаем кол-во мест хранения, должно быть 2
        Assert.assertEquals(2, storePlaceProcessing.inventoryTask.taskRepository.getStorePlace().getStorePlaces().size.toLong())
    }*/
}