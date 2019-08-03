package com.lenta.inventory

import com.lenta.inventory.di.BaseUnitTest
import com.lenta.inventory.di.DaggerTestComponent
import com.lenta.inventory.features.goods_information.excise_alco.GoodsInfoCountType
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.*
import com.lenta.shared.models.core.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class ProcessExciseAlcoProductServiceTest : BaseUnitTest() {
    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    @Inject
    lateinit var processExciseAlcoProductService: ProcessExciseAlcoProductService

    @Before
    fun setup() {
        DaggerTestComponent.builder().build().inject(this)
        creatingObjectsForTest()
    }

    private lateinit var taskDescription: TaskDescription
    private val storePlace = "00"
    private val materialNumber = "000000000000378167"
    private val boxNumber = "14680001340000845000000001"
    private lateinit var product: TaskProductInfo
    private lateinit var exciseStamp150_1: TaskExciseStamp
    private lateinit var exciseStamp150_2: TaskExciseStamp
    private lateinit var exciseStamp68_1: TaskExciseStamp
    private lateinit var exciseStamp68_2: TaskExciseStamp
    private lateinit var exciseStampsForBox_150: List<TaskExciseStamp>

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
                gis = GisControl.Alcohol,
                linkOldStamp = false
        )

        product = TaskProductInfo(
                materialNumber = materialNumber,
                description = "Вино МОЛОКО ЛЮБИМОЙ ЖЕНЩИНЫ выс.кач.обл.возд.Рейн-Гессен голуб.бут.белое п/сл. (Германия) 0.75L",
                uom = Uom("ST", "шт"),
                type = ProductType.ExciseAlcohol,
                isSet = false,
                sectionId = "01",
                matrixType = MatrixType.Active,
                materialType = "",
                placeCode = storePlace,
                factCount = 0.0,
                isPositionCalc = false,
                isExcOld = false
        )

        exciseStamp150_1 = TaskExciseStamp(
                materialNumber = materialNumber,
                code = "151869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060",
                placeCode = storePlace,
                boxNumber = "",
                setMaterialNumber = "",
                manufacturerCode = "",
                bottlingDate = "",
                isBadStamp = false)
        exciseStamp150_2 = TaskExciseStamp(
                materialNumber = materialNumber,
                code = "251869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060",
                placeCode = storePlace,
                boxNumber = "",
                setMaterialNumber = "",
                manufacturerCode = "",
                bottlingDate = "",
                isBadStamp = false)
        exciseStamp68_1 = TaskExciseStamp(
                materialNumber = materialNumber,
                code = "22N0000154KNI691XDC380V71231001511013ZZ012345678901234567890123456ZZ",
                placeCode = storePlace,
                boxNumber = "",
                setMaterialNumber = "",
                manufacturerCode = "",
                bottlingDate = "",
                isBadStamp = false)
        exciseStamp68_2 = TaskExciseStamp(
                materialNumber = materialNumber,
                code = "22N0000154KNI691XDC380V71231001513730ZZ012345678901234567890123456ZZ",
                placeCode = storePlace,
                boxNumber = "",
                setMaterialNumber = "",
                manufacturerCode = "",
                bottlingDate = "",
                isBadStamp = false)
        exciseStampsForBox_150 = listOf(
                TaskExciseStamp(
                        materialNumber = materialNumber,
                        code = "851869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060",
                        placeCode = storePlace,
                        boxNumber = boxNumber,
                        setMaterialNumber = "",
                        manufacturerCode = "",
                        bottlingDate = "",
                        isBadStamp = false),
                TaskExciseStamp(
                        materialNumber = materialNumber,
                        code = "951869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060",
                        placeCode = storePlace,
                        boxNumber = boxNumber,
                        setMaterialNumber = "",
                        manufacturerCode = "",
                        bottlingDate = "",
                        isBadStamp = false)
                )

        processServiceManager.newInventoryTask(taskDescription)

        //добавляем продукт в репозиторий
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                addProduct(TaskProductInfo.from(product, storePlace, 0.0))

        processExciseAlcoProductService.newProcessExciseAlcoProductService(TaskProductInfo.from(product, storePlace, 0.0))
    }

    @Test
    fun setFactCount() {

        //устанавливаем FactCount для продукта = 7 (нажатие на кнопку "Применить")
        processExciseAlcoProductService.setFactCount(7.0)

        //проверяем фактическое кол-во продукта, должно быть 7
        Assert.assertEquals(7.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

    }

    @Test
    fun markMissing() {

        //помечаем, что продукт отсутствует (нажатие на кнопку "Отсутствует")
        processExciseAlcoProductService.markMissing()

        //проверяем фактическое кол-во продукта, должно быть 0
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)

    }

    //for stamps 150
    @Test
    fun addCurrentExciseStamp() {

        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //проверяем фактическое кол-во продукта, должно быть 1
        Assert.assertEquals(1.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 1
        Assert.assertEquals(1, processExciseAlcoProductService.getCountVintageStamps())

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2)

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountVintageStamps())
    }

    //for box
    @Test
    fun addCurrentExciseStamps() {

        //добавляем две акцизные марки 150 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150)

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountVintageStamps())
    }

    //for stamps 68
    @Test
    fun add() {

        //добавляем первую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1)

        //проверяем фактическое кол-во продукта, должно быть 1
        Assert.assertEquals(1.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 1
        Assert.assertEquals(1, processExciseAlcoProductService.getCountPartlyStamps())

        //добавляем вторую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_2)

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountPartlyStamps())
    }

    @Test
    fun rollback() {

        //добавляем 1 акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //добавляем 1 акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1)

        //добавляем две акцизные марки 150 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150)


        //проверяем фактическое кол-во продукта, должно быть 4 (1 марка 150 символов, 1 марка 68 символов, 2 марки по 150 символов для коробки)
        Assert.assertEquals(4.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 4
        Assert.assertEquals(4, processExciseAlcoProductService.getCountVintageStamps() + processExciseAlcoProductService.getCountPartlyStamps())

        //делаем rollback
        processExciseAlcoProductService.rollback()

        //проверяем фактическое кол-во продукта, должно быть 2 (удалились добавленные две марки по 150 символов для коробки)
        Assert.assertEquals(2.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 2 (удалились добавленные две марки по 150 символов для коробки)
        Assert.assertEquals(2, processExciseAlcoProductService.getCountVintageStamps() + processExciseAlcoProductService.getCountPartlyStamps())

        //делаем еще один rollback
        processExciseAlcoProductService.rollback()

        //проверяем фактическое кол-во продукта, должно быть 1 (удалилась марка 68 символов)
        Assert.assertEquals(1.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 1 (удалилась марка 68 символов)
        Assert.assertEquals(1, processExciseAlcoProductService.getCountVintageStamps() + processExciseAlcoProductService.getCountPartlyStamps())

        //делаем еще один rollback
        processExciseAlcoProductService.rollback()

        //проверяем фактическое кол-во продукта, должно быть 0 (удалилась марка 150 символов)
        Assert.assertEquals(0.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 0 (удалилась марка 150 символов)
        Assert.assertEquals(0, processExciseAlcoProductService.getCountVintageStamps() + processExciseAlcoProductService.getCountPartlyStamps())
    }

    @Test
    fun isTaskAlreadyHasExciseStamp() {

        //проверяем добавлена ли марка, должно быть FALSE
        Assert.assertFalse(processExciseAlcoProductService.isTaskAlreadyHasExciseStamp(exciseStamp150_1.code))

        //добавляем акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1!!)

        //проверяем добавлена ли марка, должно быть TRUE
        Assert.assertTrue(processExciseAlcoProductService.isTaskAlreadyHasExciseStamp(exciseStamp150_1.code))
    }

    @Test
    fun isTaskAlreadyHasExciseStampBox() {

        //проверяем добавлены ли акцизные марки для коробки, должно быть FALSE
        Assert.assertFalse(processExciseAlcoProductService.isTaskAlreadyHasExciseStampBox(boxNumber))

        //добавляем акцизные марки 150 символов для коробки
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150)

        //проверяем добавлены ли акцизные марки для коробки, должно быть TRUE
        Assert.assertTrue(processExciseAlcoProductService.isTaskAlreadyHasExciseStampBox(boxNumber))
    }

    @Test
    fun apply() {

        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2)

        processExciseAlcoProductService.apply()

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0,
                processServiceManager.
                    getInventoryTask()!!.
                    taskRepository.
                    getProducts().
                    findProduct(materialNumber,storePlace)!!.
                    factCount,
                0.0
        )

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().getExciseStamps().size)
    }

    @Test
    fun isLinkingOldStamps() {
        Assert.assertFalse(processExciseAlcoProductService.isLinkingOldStamps())
    }

    @Test
    fun getLastCountExciseStamp() {
        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //добавляем первую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1)

        //вызываем ф-цию getLastCountExciseStamp
        var lastCountExciseStamp = processExciseAlcoProductService.getLastCountExciseStamp()

        //проверяем, кол-во должно быть 1
        Assert.assertEquals(1, lastCountExciseStamp.countLastExciseStamp)

        //проверяем, тип должен PARTLY
        Assert.assertTrue(GoodsInfoCountType.PARTLY.number == lastCountExciseStamp.countType)

        //делаем rollback, откатываем одну марку
        processExciseAlcoProductService.rollback()

        //вызываем ф-цию getLastCountExciseStamp
        lastCountExciseStamp = processExciseAlcoProductService.getLastCountExciseStamp()

        //проверяем, кол-во должно быть 1
        Assert.assertEquals(1, lastCountExciseStamp.countLastExciseStamp)

        //проверяем, тип должен быть VINTAGE
        Assert.assertTrue(GoodsInfoCountType.VINTAGE.number == lastCountExciseStamp.countType)

        //делаем rollback, откатываем еще одну марку
        processExciseAlcoProductService.rollback()

        //вызываем ф-цию getLastCountExciseStamp
        lastCountExciseStamp = processExciseAlcoProductService.getLastCountExciseStamp()

        //проверяем, кол-во должно быть 0
        Assert.assertEquals(0, lastCountExciseStamp.countLastExciseStamp)

        //проверяем, тип должен быть QUANTITY
        Assert.assertTrue(GoodsInfoCountType.QUANTITY.number == lastCountExciseStamp.countType)
    }

    @Test
    fun discard() {
        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2)

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountVintageStamps())

        //вызываем discard
        processExciseAlcoProductService.discard()

        //проверяем фактическое кол-во продукта, должно быть null
        Assert.assertTrue(processExciseAlcoProductService.getFactCount() == null)
    }

    @Test
    fun getCountPartlyStamps() {
        //добавляем первую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1)

        //добавляем вторую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_2)

        //проверяем getCountPartlyStamps, кол-во марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountPartlyStamps())
    }

    @Test
    fun getCountVintageStamps() {
        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2)

        //добавляем две акцизные марки 150 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150)

        //проверяем getCountVintageStamps, кол-во марок, должно быть 4
        Assert.assertEquals(4, processExciseAlcoProductService.getCountVintageStamps())
    }

    @Test
    fun delAllPartlyStamps() {
        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2)

        //добавляем две акцизные марки 150 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150)

        //добавляем первую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1)

        //добавляем вторую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_2)

        //проверяем кол-во партионных марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountPartlyStamps())

        //проверяем кол-во марочных марок, должно быть 4
        Assert.assertEquals(4, processExciseAlcoProductService.getCountVintageStamps())

        //удаляем все партионные марки delAllPartlyStamps
        processExciseAlcoProductService.delAllPartlyStamps()

        //проверяем кол-во партионных марок, должно быть 0
        Assert.assertEquals(0, processExciseAlcoProductService.getCountPartlyStamps())

        //проверяем кол-во марочных марок, должно быть 4
        Assert.assertEquals(4, processExciseAlcoProductService.getCountVintageStamps())
    }

    @Test
    fun delAllVintageStamps() {
        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1)

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2)

        //добавляем две акцизные марки 150 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150)

        //добавляем первую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1)

        //добавляем вторую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_2)

        //проверяем кол-во партионных марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountPartlyStamps())

        //проверяем кол-во марочных марок, должно быть 2
        Assert.assertEquals(4, processExciseAlcoProductService.getCountVintageStamps())

        //удаляем все марочные марки delAllVintageStamps
        processExciseAlcoProductService.delAllVintageStamps()

        //проверяем кол-во партионных марок, должно быть 2
        Assert.assertEquals(2, processExciseAlcoProductService.getCountPartlyStamps())

        //проверяем кол-во марочных марок, должно быть 0
        Assert.assertEquals(0, processExciseAlcoProductService.getCountVintageStamps())
    }
}