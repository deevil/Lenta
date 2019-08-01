package com.lenta.inventory

import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.InventoryTaskManager
import com.lenta.inventory.models.task.ProcessGeneralProductService
import com.lenta.inventory.models.task.TaskDescription
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import org.junit.Assert
import org.junit.Test

class ProcessGeneralProductServiceTest {

    private val materialNumber = "000000000000000021"
    lateinit var taskDescription: TaskDescription
    private var processServiceManager = InventoryTaskManager()
    private var processGeneralProductService = ProcessGeneralProductService()

    fun creatingObjectsForTest() {
        taskDescription = TaskDescription(
                taskNumber = "4485",
                taskName = "12 неделя",
                taskType = "ВИ",
                tkNumber = "0001",
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

        processServiceManager.newInventoryTask(taskDescription)
        //инициализация processServiceManager в ProcessGeneralProductService, необходимо, т.к. реализовано через DI
        processGeneralProductService.processServiceManager = processServiceManager

    }

    @Test
    fun getFactCount() {

        creatingObjectsForTest()

        var product1: TaskProductInfo? = TaskProductInfo(
                materialNumber = materialNumber,
                description = "Р/к горбуша (Россия) 230/250г",
                uom = Uom("ST", "шт"),
                type = ProductType.General,
                isSet = false,
                sectionId = "01",
                matrixType = MatrixType.Active,
                materialType = "",
                placeCode = "1",
                factCount = 5.0,
                isPositionCalc = false,
                isExcOld = false
        )

        //добавляем продукт в репозиторий
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                addProduct(product1!!)

        processGeneralProductService.newProcessGeneralProductService(product1)

        //обнуляем данный объект, чтобы не было на него ссылок и связей с ним
        product1 = null

        //проверяем фактическое кол-во продукта, должно быть 5 (отображения Итого на экране)
        Assert.assertEquals(5.0, processGeneralProductService.getFactCount(), 0.0)

    }

    @Test
    fun setFactCount() {

        creatingObjectsForTest()

        var product1: TaskProductInfo? = TaskProductInfo(
                materialNumber = materialNumber,
                description = "Р/к горбуша (Россия) 230/250г",
                uom = Uom("ST", "шт"),
                type = ProductType.General,
                isSet = false,
                sectionId = "01",
                matrixType = MatrixType.Active,
                materialType = "",
                placeCode = "1",
                factCount = 0.0,
                isPositionCalc = false,
                isExcOld = false
        )

        var product2: TaskProductInfo? = TaskProductInfo(
                materialNumber = materialNumber,
                description = "Р/к горбуша (Россия) 230/250г",
                uom = Uom("ST", "шт"),
                type = ProductType.General,
                isSet = false,
                sectionId = "01",
                matrixType = MatrixType.Active,
                materialType = "",
                placeCode = "2",
                factCount = 0.0,
                isPositionCalc = false,
                isExcOld = false
        )

        //добавляем продукт 1 в репозиторий
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                addProduct(product1!!)

        //добавляем продукт 2 в репозиторий
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                addProduct(product2!!)

        processGeneralProductService.newProcessGeneralProductService(product1)

        //обнуляем данные объект, чтобы не было на них ссылок и связей с ними
        product1 = null
        product2 = null

        //устанавливаем продукту с МХ=1 в репозитории фактическое количество (5), и помечаем, что продукт обработан
        processGeneralProductService.setFactCount(5.0)

        //проверяем кол-во продуктов, должно быть 5
        Assert.assertEquals(5.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.factCount, 0.0)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.isPositionCalc)

        //устанавливаем отрицательное кол-во продуктов -1
        processGeneralProductService.setFactCount(-1.0)

        //проверяем кол-во продуктов, должно остаться 5
        Assert.assertEquals(5.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.factCount, 0.0)

        //проверяем кол-во продуктов через ф-цию getFactCount, должно остаться 5
        Assert.assertEquals(5.0, processGeneralProductService.getFactCount(), 0.0)

        //устанавливаем кол-во продуктов в ноль
        processGeneralProductService.setFactCount(0.0)

        //проверяем кол-во продуктов, должно быть 0
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.factCount, 0.0)

        //проверяем, что продукт стал помечен как не обработанный
        Assert.assertTrue(!processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.isPositionCalc)

        //проверяем, что продукт с МХ=2 остался без изменений (factCount == 0.0)
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "2")!!.factCount, 0.0)

        //проверяем, что продукт с МХ=2 остался помечен как не обработанный
        Assert.assertFalse(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "2")!!.isPositionCalc)

    }

    @Test
    fun markMissing() {

        creatingObjectsForTest()

        var product1: TaskProductInfo? = TaskProductInfo(
                materialNumber = materialNumber,
                description = "Р/к горбуша (Россия) 230/250г",
                uom = Uom("ST", "шт"),
                type = ProductType.General,
                isSet = false,
                sectionId = "01",
                matrixType = MatrixType.Active,
                materialType = "",
                placeCode = "00",
                factCount = 0.0,
                isPositionCalc = false,
                isExcOld = false
        )

        //добавляем продукт в репозиторий
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                addProduct(product1!!)

        processGeneralProductService.newProcessGeneralProductService(product1)

        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        product1 = null

        //помечаем, что продукт отсутствует
        processGeneralProductService.markMissing()

        //проверяем кол-во продуктов, должно быть 0
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "00")!!.factCount, 0.0)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "00")!!.isPositionCalc)

    }
}