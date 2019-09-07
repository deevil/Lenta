package com.lenta.inventory

import com.lenta.inventory.di.BaseUnitTest
import com.lenta.inventory.di.DaggerTestComponent
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.*
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class ProcessGeneralProductServiceTest : BaseUnitTest() {

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    @Inject
    lateinit var processGeneralProductService: ProcessGeneralProductService

    @Before
    fun setup() {
        DaggerTestComponent.builder().build().inject(this)
        creatingObjectsForTest()
    }

    private val materialNumber = "000000000000000021"
    lateinit var taskDescription: TaskDescription

    fun creatingObjectsForTest() {
        taskDescription = TaskDescription(
                taskNumber = "4485",
                taskName = "12 неделя",
                taskType = "ВИ",
                tkNumber = "0001",
                ivCountPerNr = true,
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
                linkOldStamp = true,
                processingEndTime = null,
                isRecount = false
        )

        processServiceManager.newInventoryTask(taskDescription)

    }

    @Test
    fun getFactCount() {

        val product1 = TaskProductInfo(
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
                addProduct(product1)

        processGeneralProductService.newProcessGeneralProductService(product1)

        //проверяем фактическое кол-во продукта, должно быть 5 (отображения Итого на экране)
        Assert.assertEquals(5.0, processGeneralProductService.getFactCount(), 0.0)

    }

    @Test
    fun setFactCount() {

        val product1 = TaskProductInfo(
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

        val product2 = TaskProductInfo(
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
                addProduct(product1)

        //добавляем продукт 2 в репозиторий
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                addProduct(product2)

        processGeneralProductService.newProcessGeneralProductService(product1)

        //устанавливаем продукту с МХ=1 в репозитории фактическое количество (3), и помечаем, что продукт обработан
        processGeneralProductService.setFactCount(3.0)

        //проверяем кол-во продуктов, должно быть 3
        Assert.assertEquals(3.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.factCount, 0.0)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.isPositionCalc)

        //устанавливаем отрицательное кол-во продуктов -1
        processGeneralProductService.setFactCount(-1.0)

        //проверяем кол-во продуктов, должно остаться 3
        Assert.assertEquals(3.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "1")!!.factCount, 0.0)

        //проверяем кол-во продуктов через ф-цию getFactCount, должно остаться 0
        Assert.assertEquals(0.0, processGeneralProductService.getFactCount(), 0.0)

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

        //проверяем, что в репозитории 2 продукта
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().getProducts().size == 2)
    }

    @Test
    fun markMissing() {

        val product1 = TaskProductInfo(
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
                addProduct(product1)

        processGeneralProductService.newProcessGeneralProductService(product1)

        //проверяем кол-во продуктов, должно быть 0
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "00")!!.factCount, 0.0)

        //проверяем, что продукт помечен как не обработанный
        Assert.assertFalse(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "00")!!.isPositionCalc)

        //устанавливаем FactCount для продукта = 7 (чтобы потом проверить, что стало 0)
        processGeneralProductService.setFactCount(7.0)

        //помечаем, что продукт отсутствует
        processGeneralProductService.markMissing()

        //проверяем кол-во продуктов, должно быть 0
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "00")!!.factCount, 0.0)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, "00")!!.isPositionCalc)

    }

}