package com.lenta.inventory

import com.lenta.inventory.features.goods_information.excise_alco.GoodsInfoCountType
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.*
import com.lenta.shared.models.core.*
import org.junit.Assert
import org.junit.Test

class ProcessExciseAlcoProductServiceTest {
    private lateinit var taskDescription: TaskDescription
    private var processServiceManager = InventoryTaskManager()
    private val storePlace = "00"
    private val materialNumber = "000000000000378167"
    private val boxNumber = "14680001340000845000000001"
    private var product: TaskProductInfo? = null
    private var exciseStamp150_1: TaskExciseStamp? =null
    private var exciseStamp150_2: TaskExciseStamp? =null
    private var exciseStamp68_1: TaskExciseStamp? =null
    private var exciseStamp68_2: TaskExciseStamp? =null
    private var exciseStampsForBox_150: List<TaskExciseStamp>? =null
    private var exciseStampsForBox_68: List<TaskExciseStamp>? =null
    private var processExciseAlcoProductService = ProcessExciseAlcoProductService()

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

        exciseStampsForBox_68 = listOf(
                TaskExciseStamp(
                        materialNumber = materialNumber,
                        code = "32N0000154KNI691XDC380V71231001511013ZZ012345678901234567890123456ZZ",
                        placeCode = storePlace,
                        boxNumber = boxNumber,
                        setMaterialNumber = "",
                        manufacturerCode = "",
                        bottlingDate = "",
                        isBadStamp = false),
                TaskExciseStamp(
                        materialNumber = materialNumber,
                        code = "42N0000154KNI691XDC380V71231001513730ZZ012345678901234567890123456ZZ",
                        placeCode = storePlace,
                        boxNumber = boxNumber,
                        setMaterialNumber = "",
                        manufacturerCode = "",
                        bottlingDate = "",
                        isBadStamp = false)
        )

        processServiceManager.newInventoryTask(taskDescription)
        //инициализация processServiceManager в ProcessGeneralProductService, необходимо, т.к. реализовано через DI
        processExciseAlcoProductService.processServiceManager = processServiceManager

        processExciseAlcoProductService.newProcessExciseAlcoProductService(product!!)

        //добавляем продукт в репозиторий
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                addProduct(product!!)

        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        product = null



    }

    @Test
    fun getFactCount() {

        creatingObjectsForTest()

        //устанавливаем продукту FactCount=5, имитация, что продукту ранее установили кол-во = 5, и потом проверяем работу отображения Итого на экране
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount = 5.0

        //проверяем фактическое кол-во продукта, должно быть 5
        Assert.assertEquals(5.0, processExciseAlcoProductService.getFactCount()!!, 0.0)

    }

    @Test
    fun setFactCount() {

        creatingObjectsForTest()

        //устанавливаем FactCount для продукта = 7 (нажатие на кнопку "Применить")
        processExciseAlcoProductService.setFactCount(7.0)

        //проверяем фактическое кол-во продукта, должно быть 7
        Assert.assertEquals(7.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

    }

    @Test
    fun markMissing() {

        creatingObjectsForTest()

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

        creatingObjectsForTest()

        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_1 = null

        //проверяем фактическое кол-во продукта, должно быть 1
        Assert.assertEquals(1.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 1
        Assert.assertEquals(1, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)


        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_2 = null

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)

    }

    //for box
    @Test
    fun addCurrentExciseStamps() {

        creatingObjectsForTest()

        //добавляем две акцизные марки 150 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStampsForBox_150 = null

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)
    }

    //for stamps 68
    @Test
    fun add() {

        creatingObjectsForTest()

        //добавляем первую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp68_1 = null

        //проверяем фактическое кол-во продукта, должно быть 1
        Assert.assertEquals(1.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 1
        Assert.assertEquals(1, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)


        //добавляем вторую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_2!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp68_2 = null

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)

    }

    @Test
    fun rollback() {

        creatingObjectsForTest()

        //добавляем 1 акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1!!)

        //добавляем 1 акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1!!)

        //добавляем две акцизные марки 150 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150!!)

        //добавляем две акцизные марки 68 символов для коробки продукту
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_68!!)

        //processExciseAlcoProductService.addCurrentExciseStamps(listOf(exciseStamp150_1!!, exciseStamp150_2!!, exciseStamp68_1!!, exciseStamp68_2!!))
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_1 = null
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp68_1 = null
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStampsForBox_150 = null
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStampsForBox_68 = null

        //проверяем фактическое кол-во продукта, должно быть 6 (1 марка по 150 символов, 1 марка по 68 символов, 2 марки по 150 символов для коробки, 2 марки по 68 символов для коробки
        Assert.assertEquals(6.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 6
        Assert.assertEquals(6, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //делаем rollback
        processExciseAlcoProductService.rollback()

        //проверяем фактическое кол-во продукта, должно быть 4 (удалились добавленные две марки по 68 символов для коробки)
        Assert.assertEquals(4.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 4 (удалились добавленные две марки по 68 символов для коробки)
        Assert.assertEquals(4, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //делаем еще один rollback
        processExciseAlcoProductService.rollback()

        //проверяем фактическое кол-во продукта, должно быть 2 (удалились добавленные две марки по 150 символов для коробки)
        Assert.assertEquals(2.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 2 (удалились добавленные две марки по 150 символов для коробки)
        Assert.assertEquals(2, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //делаем еще один rollback
        processExciseAlcoProductService.rollback()

        //проверяем фактическое кол-во продукта, должно быть 1 (удалилась марка 68 символов)
        Assert.assertEquals(1.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 1 (удалилась марка 68 символов)
        Assert.assertEquals(1, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //делаем еще один rollback
        processExciseAlcoProductService.rollback()

        //проверяем фактическое кол-во продукта, должно быть 0 (удалилась марка 150 символов)
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 1 (удалилась марка 150 символов)
        Assert.assertEquals(0, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)
    }

    @Test
    fun isTaskAlreadyHasExciseStamp() {

        creatingObjectsForTest()

        val exciseStamp = exciseStamp150_1!!.code

        //проверяем добавлена ли эта марка, должно быть FALSE
        Assert.assertFalse(processExciseAlcoProductService.isTaskAlreadyHasExciseStamp(exciseStamp))

        //добавляем акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_1 = null

        //проверяем добавлена ли эта марка, должно быть TRUE
        Assert.assertTrue(processExciseAlcoProductService.isTaskAlreadyHasExciseStamp(exciseStamp))
    }

    @Test
    fun isTaskAlreadyHasExciseStampBox() {

        creatingObjectsForTest()

        //проверяем добавлены ли акцизные марки для коробки, должно быть FALSE
        Assert.assertFalse(processExciseAlcoProductService.isTaskAlreadyHasExciseStampBox(boxNumber))

        //добавляем акцизные марки 150 символов для коробки
        processExciseAlcoProductService.addCurrentExciseStamps(exciseStampsForBox_150!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStampsForBox_150 = null

        //проверяем добавлены ли акцизные марки для коробки, должно быть TRUE
        Assert.assertTrue(processExciseAlcoProductService.isTaskAlreadyHasExciseStampBox(boxNumber))
    }

    @Test
    fun apply() {

        creatingObjectsForTest()

        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_1 = null

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_2 = null

        processExciseAlcoProductService.apply()

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)
    }

    @Test
    fun isLinkingOldStamps() {
        creatingObjectsForTest()
        Assert.assertFalse(processExciseAlcoProductService.isLinkingOldStamps())
    }

    @Test
    fun getLastCountExciseStamp() {
        creatingObjectsForTest()

        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_1 = null

        //добавляем первую акцизную марку 68 символов продукту
        processExciseAlcoProductService.add(1, exciseStamp68_1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp68_1 = null

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
        creatingObjectsForTest()

        //добавляем первую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_1!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_1 = null

        //добавляем вторую акцизную марку 150 символов продукту
        processExciseAlcoProductService.addCurrentExciseStamp(exciseStamp150_2!!)
        //обнуляем  данный объект, чтобы не было на него ссылок и связей с ним
        exciseStamp150_2 = null

        //проверяем фактическое кол-во продукта, должно быть 2
        Assert.assertEquals(2.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 2
        Assert.assertEquals(2, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как обработанный
        Assert.assertTrue(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)

        //вызываем discard
        processExciseAlcoProductService.discard()

        //проверяем фактическое кол-во продукта, должно быть 0
        Assert.assertEquals(0.0, processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.factCount, 0.0)

        //проверяем кол-во марок, должно быть 0
        Assert.assertEquals(0, processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(materialNumber,storePlace,false).size)

        //проверяем, что продукт помечен как не обработанный
        Assert.assertFalse(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(materialNumber, storePlace)!!.isPositionCalc)

    }
}