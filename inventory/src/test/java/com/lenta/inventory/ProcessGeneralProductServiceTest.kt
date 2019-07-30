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

class ProcessGeneralProductServiceTest {

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
                gis = GisControl.GeneralProduct,
                linkOldStamp = true
        )

        inventoryTask = InventoryTask(taskDescription, taskRepository = MemoryTaskRepository())
        storePlaceProcessing = StorePlaceProcessing(inventoryTask, storePlaceNumber = "123456789")
    }



    @Test
    fun testProcessGeneralProduct() {

        creatingObjectsForTest()

        var test = false

        val product1 = TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", 0.0, isPositionCalc = false, isExcOld = false)

        val product2 = TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, "1", MatrixType.Active, "materialType","2", 0.0, isPositionCalc = false, isExcOld = false)

        val product3 = TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, "1", MatrixType.Active, "materialType", "3", 0.0, isPositionCalc = false, isExcOld = false)

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

    @Test
    fun testSetFactCountForProduct() {

        creatingObjectsForTest()

        var product1: TaskProductInfo? = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", 0.0, isPositionCalc = false, isExcOld = false)

        var product2: TaskProductInfo? = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "2", 0.0, isPositionCalc = false, isExcOld = false)

        //добавляем продукты в репозиторий
        inventoryTask.taskRepository.getProducts().addProduct(product1!!)
        inventoryTask.taskRepository.getProducts().addProduct(product2!!)
        //обнуляем  данные объект, чтобы не было на него ссылок и связей с ним
        product1 = null
        product2 = null

        val processGeneralProductService = ProcessGeneralProductService(taskDescription, inventoryTask.taskRepository, inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!)
        //устанавливаем продукту с МХ=1 в репозитории фактическое количество (5), и помечаем, что продукт обработан
        processGeneralProductService.setFactCount(5.0)

        //проверяем кол-во продуктов, должно быть 5
        Assert.assertEquals(5.0, inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!.factCount, 0.0)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!.isPositionCalc)

        //устанавливаем отрицательное кол-во продуктов -1
        processGeneralProductService.setFactCount(-1.0)
        //проверяем кол-во продуктов, должно остаться 5
        Assert.assertEquals(5.0, inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!.factCount, 0.0)
        //проверяем кол-во продуктов через ф-цию getFactCount, должно остаться 5
        Assert.assertEquals(5.0, processGeneralProductService.getFactCount(), 0.0)

        //устанавливаем кол-во продуктов в ноль
        processGeneralProductService.setFactCount(0.0)
        //проверяем кол-во продуктов, должно быть 0
        Assert.assertEquals(0.0, inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!.factCount, 0.0)
        //проверяем, что продукт стал помечен как необработанный
        Assert.assertTrue(!inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!.isPositionCalc)

        //проверяем, что продукт с МХ=2 остался без изменений
        var test = false
        if (inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "2")!!.factCount == 0.0) {
            test = true
        }
        Assert.assertTrue(test)
        Assert.assertFalse(inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "2")!!.isPositionCalc)

    }

    @Test
    fun testMarkMissingForProduct() {

        creatingObjectsForTest()

        var product1: TaskProductInfo? = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", 0.0, isPositionCalc = false, isExcOld = false)

        //добавляем продукт в репозиторий
        inventoryTask.taskRepository.getProducts().addProduct(product1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        product1 = null

        val processGeneralProductService = ProcessGeneralProductService(taskDescription, inventoryTask.taskRepository, inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!)

        //помечаем, что продукт отсутствует
        processGeneralProductService.markMissing()

        //проверяем кол-во продуктов, должно быть 0
        Assert.assertEquals(0.0, inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!.factCount, 0.0)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1")!!.isPositionCalc)

    }

    @Test
    fun testDeleteProduct() {

        creatingObjectsForTest()

        var product1: TaskProductInfo? = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "1", 0.0, isPositionCalc = false, isExcOld = false)

        var product2: TaskProductInfo? = TaskProductInfo("materialNumber111", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType", "2", 0.0, isPositionCalc = false, isExcOld = false)

        //добавляем продукты в репозиторий
        inventoryTask.taskRepository.getProducts().addProduct(product1!!)
        inventoryTask.taskRepository.getProducts().addProduct(product2!!)

        //удаляем продукт с МХ=1 (дублируем удаление несколько раз)
        inventoryTask.taskRepository.getProducts().deleteProduct(product1!!)

        var test = false
        if (inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "1") == null) {
            test = true
        }
        Assert.assertTrue(test)

        //продублируем удаление продукта с МХ=1 несколько раз, если код удаления не верен, то после второго удаления продукта с МХ=1, может удалиться и продукт с МХ=2
        inventoryTask.taskRepository.getProducts().deleteProduct(product1!!)
        inventoryTask.taskRepository.getProducts().deleteProduct(product1!!)
        inventoryTask.taskRepository.getProducts().deleteProduct(product1!!)
        product1 = null
        product2 = null
        //проверяем,, продукт с МХ=2 должен быть в репозитории
        test = false
        if (inventoryTask.taskRepository.getProducts().findProduct("materialNumber111", "2") != null) {
            test = true
        }
        Assert.assertTrue(test)
    }
}