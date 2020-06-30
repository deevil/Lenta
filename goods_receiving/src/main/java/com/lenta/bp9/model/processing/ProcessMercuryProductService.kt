package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToLong

const val PROCESSING_MERCURY_UNKNOWN = 0
const val PROCESSING_MERCURY_SAVED = 1
const val PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC = 2
const val PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE = 3
const val PROCESSING_MERCURY_SURPLUS_IN_QUANTITY = 4
const val PROCESSING_MERCURY_UNDERLOAD_AND_SURPLUS_IN_ONE_DELIVERY = 5
const val PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_INVOICE = 6
const val PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_VET_DOC = 7
const val PROCESSING_MERCURY_ROUND_QUANTITY_TO_PLANNED = 8
const val PROCESSING_MERCURY_CANT_SAVE_NEGATIVE_NUMBER = 9
const val PROCESSING_MERCURY_OVERDELIVERY_MORE_EQUAL_NOT_ORDER = 10
const val PROCESSING_MERCURY_OVERDELIVERY_LESS_NOT_ORDER = 11

@AppScope
class ProcessMercuryProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    private lateinit var productInfo: TaskProductInfo
    private val newProductDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private val newVetProductDiscrepancies: ArrayList<TaskMercuryDiscrepancies> = ArrayList()
    private var isModifications: Boolean = false

    fun newProcessMercuryProductService(productInfo: TaskProductInfo) : ProcessMercuryProductService? {
        return if (productInfo.isVet){
            this.productInfo = productInfo.copy()
            newProductDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.map {
                newProductDiscrepancies.add(it.copy())
            }
            newVetProductDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryDiscrepanciesOfProduct(productInfo)?.map {
                newVetProductDiscrepancies.add(it)
            }
            isModifications = false
            this
        }
        else null
    }

    fun add(count: String, isConvertUnit: Boolean, typeDiscrepancies: String, manufacturer: String, productionDate: String){
        isModifications = true
        val countAdd = getNewCountByDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()

        //добавляем кол-во по расхождению для продукта
        var foundDiscrepancy = newProductDiscrepancies.findLast {
            it.typeDiscrepancies == typeDiscrepancies
        }

        foundDiscrepancy = foundDiscrepancy?.copy(numberDiscrepancies = countAdd.toString(), processingUnitNumber = productInfo.processingUnit)
                ?: TaskProductDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        processingUnitNumber = productInfo.processingUnit,
                        numberDiscrepancies = countAdd.toString(),
                        uom = productInfo.uom,
                        typeDiscrepancies = typeDiscrepancies,
                        isNotEdit = false,
                        isNew = productInfo.isGoodsAddedAsSurplus,
                        notEditNumberDiscrepancies = ""
                )

        changeNewProductDiscrepancy(foundDiscrepancy)

        //добавляем кол-во по расхождению для ВСД
        val vetDocumentVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0
        val vetDocumentCountAlreadyAdded = newVetProductDiscrepancies.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate &&
                    it.typeDiscrepancies == typeDiscrepancies
        }.sumByDouble {
            it.numberDiscrepancies
        }
        var countAllAddVetDoc = if (isConvertUnit) convertUnitForPGEVsd(countAdd) else countAdd + vetDocumentCountAlreadyAdded

        val foundVetDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.map {
            val addCount: Double
            if (vetDocumentVolume <= countAllAddVetDoc && countAllAddVetDoc > 0.0) {
                addCount = vetDocumentVolume
                countAllAddVetDoc -= vetDocumentVolume
            } else {
                addCount = countAllAddVetDoc
                countAllAddVetDoc = 0.0
            }

            TaskMercuryDiscrepancies(
                    materialNumber = it.materialNumber,
                    vetDocumentID = it.vetDocumentID,
                    volume = it.volume,
                    uom = productInfo.uom,
                    typeDiscrepancies = typeDiscrepancies,
                    numberDiscrepancies = addCount,
                    productionDate = it.productionDate,
                    manufacturer = it.manufacturer,
                    productionDateTo = it.productionDateTo
            )
        }

        foundVetDiscrepancy?.map {
            changeNewVetProductDiscrepancy(it)
        }
    }

    fun addSurplusInQuantityPGE(count: Double, isConvertUnit: Boolean, manufacturer: String, productionDate: String) {
        val countNormAdd = productInfo.orderQuantity.toDouble() - (getQuantityAllCategoryExceptNonOrderOfProductPGE(count) - count)
        val countSurplusAdd = count - countNormAdd
        if (countNormAdd > 0.0) {
            add(countNormAdd.toString(), isConvertUnit, "1", manufacturer, productionDate)
        }
        if (countSurplusAdd > 0.0) {
            add(countSurplusAdd.toString(), isConvertUnit, "2", manufacturer, productionDate)
        }
    }

    fun addNormAndUnderloadExceededInvoicePGE(count: Double, isConvertUnit: Boolean, manufacturer: String, productionDate: String) {
        val countExceeded = getQuantityAllCategoryExceptNonOrderOfProductPGE(count) - productInfo.orderQuantity.toDouble()

        //удаляем превышающее количество из недогруза в текущей ВСД
        var delCount: Double = if (isConvertUnit) convertUnitForPGEVsd(countExceeded) else countExceeded
        newVetProductDiscrepancies.filter {newVet ->
            newVet.typeDiscrepancies == "3" && newVet.manufacturer == manufacturer && newVet.productionDate == productionDate
        }.map {
            if ((it.numberDiscrepancies - delCount) < 0.0) {
                newVetProductDiscrepancies.remove(it)
                delCount -= it.numberDiscrepancies
            } else {
                if ((it.numberDiscrepancies - delCount) > 0.0 ) {
                    changeNewVetProductDiscrepancy(it.copy(numberDiscrepancies = it.numberDiscrepancies - delCount))
                } else {
                    newVetProductDiscrepancies.remove(it)
                }
                delCount -= it.numberDiscrepancies
            }
        }

        //у продукта удаляем превышающее количество из недогруза
        newProductDiscrepancies.findLast {it.typeDiscrepancies == "3"}?.let {
            if ((it.numberDiscrepancies.toDouble() - countExceeded) > 0.0) {
                changeNewProductDiscrepancy(it.copy(numberDiscrepancies = (it.numberDiscrepancies.toDouble() - countExceeded).toString() ))
            } else {
                newProductDiscrepancies.remove(it)
            }
        }

        //добавляем в Норму превышающее количество из недогруза кол-во до максимума по инфойсу(productInfo.orderQuantity)
        add(count.toString(), isConvertUnit, "1", manufacturer, productionDate)
    }

    fun addNormAndUnderloadExceededVetDocPGE(count: Double, isConvertUnit: Boolean, manufacturer: String, productionDate: String) {
        val vetDocumentVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        val countExceeded = getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) - vetDocumentVolume
        val countUnderloadVetDoc = newVetProductDiscrepancies.filter {
            it.typeDiscrepancies == "3" && it.manufacturer == manufacturer && it.productionDate == productionDate
        }.sumByDouble {
            it.numberDiscrepancies
        } //кол-во расхождений по недогрузу считаем через фильтр, т.к. ВСД могут быть одинаковые по производителю и дате (см. vetDocumentVolume в ф-ции add() )

        //удаляем расхождения по недогрузу из текущей ВСД
        newVetProductDiscrepancies.removeAll(newVetProductDiscrepancies.filter {
            it.typeDiscrepancies == "3" && it.manufacturer == manufacturer && it.productionDate == productionDate
        })

        //у продукта удаляем кол-во недогруза указанного в текущей ВСД
        val countUnderloadProduct = if (isConvertUnit) convertUnitForPGEProduct(countUnderloadVetDoc) else countUnderloadVetDoc
        newProductDiscrepancies.findLast {it.typeDiscrepancies == "3"}?.let {
            if ((it.numberDiscrepancies.toDouble() - countUnderloadProduct) > 0.0) {
                changeNewProductDiscrepancy(it.copy(numberDiscrepancies = (it.numberDiscrepancies.toDouble() - countUnderloadProduct).toString() ))
            } else {
                newProductDiscrepancies.remove(it)
            }
        }

        //после удаления недогруза подсчитываем оставшееся кол-во по текущей ВСД
        val vetDocumentCountAlreadyAdded = newVetProductDiscrepancies.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }.sumByDouble {
            it.numberDiscrepancies
        }

        //весь, обработанный раннее товар из категории «Недогруз», сохраняем в категорию «Норма», а превышающее количество в Излишек
        val countNormAdd = vetDocumentVolume - vetDocumentCountAlreadyAdded
        add(if (isConvertUnit) convertUnitForPGEProduct(countNormAdd).toString() else countNormAdd.toString(), isConvertUnit,  "1", manufacturer, productionDate)
        add(if (isConvertUnit) convertUnitForPGEProduct(countExceeded).toString() else countExceeded.toString(), isConvertUnit, "2", manufacturer, productionDate)
    }

    fun delDiscrepancy(typeDiscrepancies: String) {
        val delProductDiscrepancies = ArrayList<TaskProductDiscrepancies>()
        for (i in newProductDiscrepancies.indices) {
            if (typeDiscrepancies == newProductDiscrepancies[i].typeDiscrepancies) {
                delProductDiscrepancies.add(newProductDiscrepancies[i])
            }
        }
        delProductDiscrepancies.map {
            if (it.isNotEdit) { //не редактируемое расхождение https://trello.com/c/Mo9AqreT
                newProductDiscrepancies.remove(it)
                newProductDiscrepancies.add(it)
            } else {
                newProductDiscrepancies.remove(it)
            }
        }

        val delVetProductDiscrepancies = ArrayList<TaskMercuryDiscrepancies>()
        for (i in newVetProductDiscrepancies.indices) {
            if (typeDiscrepancies == newVetProductDiscrepancies[i].typeDiscrepancies) {
                delVetProductDiscrepancies.add(newVetProductDiscrepancies[i])
            }
        }
        if (delVetProductDiscrepancies.isNotEmpty()) {
            newVetProductDiscrepancies.removeAll(delVetProductDiscrepancies)
        }
    }

    fun save(){
        if (newProductDiscrepancies.isNotEmpty()) {
            newProductDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getProductsDiscrepancies()?.
                        changeProductDiscrepancy(it)
            }
        }

        if (newVetProductDiscrepancies.isNotEmpty()) {
            newVetProductDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getMercuryDiscrepancies()?.
                        changeMercuryDiscrepancy(it)
            }
        }
    }

    private fun changeNewProductDiscrepancy(newDiscrepancy: TaskProductDiscrepancies) {
        var index = -1
        for (i in newProductDiscrepancies.indices) {
            if (newDiscrepancy.typeDiscrepancies == newProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index != -1) {
            newProductDiscrepancies.removeAt(index)
        }

        newProductDiscrepancies.add(newDiscrepancy)
    }

    private fun changeNewVetProductDiscrepancy(newVetDiscrepancy: TaskMercuryDiscrepancies) {
        var index = -1
        for (i in newVetProductDiscrepancies.indices) {
            if (newVetDiscrepancy.manufacturer == newVetProductDiscrepancies[i].manufacturer &&
                    newVetDiscrepancy.productionDate == newVetProductDiscrepancies[i].productionDate &&
                    newVetDiscrepancy.vetDocumentID == newVetProductDiscrepancies[i].vetDocumentID &&
                    newVetDiscrepancy.typeDiscrepancies == newVetProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index != -1) {
            newVetProductDiscrepancies.removeAt(index)
        }

        newVetProductDiscrepancies.add(newVetDiscrepancy)
    }

    fun checkConditionsOfPreservationOfProduct(count: String,
                                               typeDiscrepancies: String,
                                               manufacturer: String,
                                               productionDate: String,
                                               paramGrzRoundLackRatio: Double,
                                               paramGrzRoundLackUnit: Double,
                                               paramGrzRoundHeapRatio: Double) : Int {

        //суммарное кол-во по ВСД (ET_VET_DIFF, поле VSDVOLUME)
        val vetDocumentIDVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        //1.1. Проверяем есть ли разница между плановым кол-ом в ВП и суммой фактически введенного кол-ва с учетом всех введенных категорий, за исключением категории “Незаказ”
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(if (typeDiscrepancies != "41") count.toDouble() else 0.0)
        if (productCategoriesSum < productInfo.origQuantity.toDouble() ) {
            val diff = productInfo.origQuantity.toDouble() - productCategoriesSum
            // выявили Недогруз, переходим к проверке на микроокругление (пункт 1.1.1).
            //1.1.1. Рассчитываем кол-во с % погрешности, используя следующую формулу:
            //Значение из поля «плановое кол-во» умножаем на значение из параметра GRZ_ROUND_LACK_RATIO.
            val acceptableRelError = productInfo.origQuantity.toDouble() * paramGrzRoundLackRatio

            //1.1.2 Кол-во недогруза сравниваем со значение, полученным в п. 1.1.1 значением:
            if (diff <= acceptableRelError) {
                //- в случае если полученное значение меньше или равно значению из п. 1.1.1, то переходим к пункту 1.1.3.
                //1.1.3 Если значение меньше или равно значению из  GRZ_ROUND_LACK_UNIT то запоминаем кол-во с округлением и переходим к пункту 1.3.
                if (diff <= paramGrzRoundLackUnit) {
                    //1.3 Выполняем проверку округленного кол-ва(плановое количество), сравниваем его с суммарным кол-вом по ВСД
                    if (productInfo.origQuantity.toDouble() <= vetDocumentIDVolume) {
                        //если сумма <= суммарному кол-ву по ВСД то выводить экран с сообщением “Вы хотите округлить кол-во до планового?”.
                        return PROCESSING_MERCURY_ROUND_QUANTITY_TO_PLANNED
                    } else {
                        //Если условие не выполнено, система отображает ошибку - «Введенное кол-во больше чем в ВСД, измените кол-во».
                        return PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC
                    }
                } else {
                    //- в случае если полученное значение больше значения из п. 1.1.3., то переходим к пункту 2.
                    return checkConditionsOfPreservationOfVSD(count, typeDiscrepancies, manufacturer, productionDate)
                }
            } else {
                //- в случае если полученное значение больше значения из п. 1.1.1., то переходим к пункту 2.
                return checkConditionsOfPreservationOfVSD(count, typeDiscrepancies, manufacturer, productionDate)
            }
        } else if (productCategoriesSum > productInfo.origQuantity.toDouble()) {
            val diff = productCategoriesSum - productInfo.origQuantity.toDouble()
            // выявили Излишек, переходим к пункту 1.2.1
            //1.2.1. Рассчитываем кол-во с % погрешности, используя следующую формулу:
            //Значение из поля «плановое кол-во» умножаем на значение из параметра GRZ_ROUND_HEAP_RATIO.
            val acceptableRelError = productInfo.origQuantity.toDouble() * paramGrzRoundHeapRatio

            //1.2.2. Кол-во излишка сравниваем со значение, полученным в п. 1.2.1 значением:
            if (diff <= acceptableRelError) {
                //1.3 Выполняем проверку округленного кол-ва(плановое количество), сравниваем его с суммарным кол-вом по ВСД
                if (productInfo.origQuantity.toDouble() <= vetDocumentIDVolume) {
                    //если сумма <= суммарному кол-ву по ВСД то выводить экран с сообщением “Вы хотите округлить кол-во до планового?”.
                    return PROCESSING_MERCURY_ROUND_QUANTITY_TO_PLANNED
                } else {
                    //Если условие не выполнено, система отображает ошибку - «Введенное кол-во больше чем в ВСД, измените кол-во».
                    return PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC
                }
            } else {
                //- в случае если полученное значение больше значения из п. 1.2.1., то переходим к пункту 2.
                return checkConditionsOfPreservationOfVSD(count, typeDiscrepancies, manufacturer, productionDate)
            }
        } else {
            //- в случае если разницы между плановым кол-ом в ВП и суммой фактически введенного кол-ва с учетом всех введенных категорий, за исключением категории “Незаказ”, нет, то переходим к пункту 2.
            return checkConditionsOfPreservationOfVSD(count, typeDiscrepancies, manufacturer, productionDate)
        }
    }

    fun checkConditionsOfPreservationOfVSD(count: String, typeDiscrepancies: String, manufacturer: String, productionDate: String) : Int {
        //суммарное кол-во по ВСД (ET_VET_DIFF, поле VSDVOLUME)
        val vetDocumentIDVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        //Сумма всех заявленных категорий по текущему ВСД за исключением «Незаказ»
        val batchCategoriesSum = getQuantityAllCategoryExceptNonOrderOfVetDoc(if (typeDiscrepancies != "41") count.toDouble() else 0.0, manufacturer, productionDate)
        // 2. Сумма всех заявленных категорий по текущему ВСД за исключением «Незаказ» <= суммарному кол-ву по ВСД (ET_VET_DIFF, поле VSDVOLUME).
        if (batchCategoriesSum < 0) {
            return PROCESSING_MERCURY_CANT_SAVE_NEGATIVE_NUMBER
        }

        // 2. Сумма всех заявленных категорий по текущему ВСД за исключением «Незаказ» <= суммарному кол-ву по ВСД (ET_VET_DIFF, поле VSDVOLUME).
        if (batchCategoriesSum > vetDocumentIDVolume) {
            //Если условие не выполнено, система отображает ошибку - «Введенное кол-во больше чем в ВСД, измените кол-во».
            return PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC
        }

        //Если условие выше выполнено, переходить к п.3
        val notOrderCount = getQuantityNonOrderOfProduct(if (typeDiscrepancies == "41") count.toDouble() else 0.0)
        // 3. Сумма всех заявленных категорий по текущему ВСД за исключением «Незаказ» <= количеству в ВП за вычитом категории незаказ(ET_TASK_POS, поле MENGE)
        if (batchCategoriesSum <= productInfo.origQuantity.toDouble() - notOrderCount) {
            //Если условие выполнено - сохранять результат пересчета.
            return PROCESSING_MERCURY_SAVED
        }

        //В случае если кол-во в поставке меньше кол-ва в заказе, то при вводе кол-ва больше чем в ВП выводим ошибку о превышении кол-ва в ВП.
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(if (typeDiscrepancies != "41") count.toDouble() else 0.0)
        if (productInfo.origQuantity.toDouble() < productInfo.orderQuantity.toDouble() && productCategoriesSum > productInfo.origQuantity.toDouble()) {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }

        //Если условие выше не выполнено и MEINS = шт. - отображать ошибку «Введенное кол-во больше чем в ВП, измените кол-во».
        if (productInfo.uom.code == "ST") {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }

        //Если условие выше не выполнено и MEINS = г. - переходить к п. 4
        val overSupply = productInfo.orderQuantity.toDouble() + (productInfo.overdToleranceLimit.toDouble() / 100) * productInfo.orderQuantity.toDouble()
        // 4. Сумма всех заявленных категорий за исключением «Незаказ» <= плановому кол-ву в заказе + кол-во в рамках сверхпоставки (UEBTO).
        if (productCategoriesSum > overSupply) {
            //Если условие не выполнено, система отображает ошибку - «Введенное кол-во больше чем в ВП, измените кол-во».
            return PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }

        //Если условие выполнено, переходим к п.5.
        // 5. Сумма всех заявленных категорий за исключением «Незаказ» <= плановому кол-ву в заказе + кол-во в рамках сверхпоставки (UEBTO):
        if (productCategoriesSum <= overSupply) {
            // - Проверить наличие категории "Незаказ"
            // - Если по товару есть категория "Незаказ"
            if (getQuantityNonOrderOfProduct(0.0) > 0.0) {
                //выполнить переливание из категории "Незаказ" в категорию "Норма"
                //в размере кол-ва введенного в рамках сверхпоставки (расчет введенного количества сверхпоставки как сумма всех заявленных категорий за вычетом количества заказа):
                val delta = productCategoriesSum - productInfo.orderQuantity.toDouble()
                val notOrderedCategory = newProductDiscrepancies.first { it.typeDiscrepancies == "41"}

                //В случае если кол-во сверхпоставки >= кол-во категории "Незаказ", то:
                if (delta >= notOrderedCategory.numberDiscrepancies.toDouble()) {
                    return PROCESSING_MERCURY_OVERDELIVERY_MORE_EQUAL_NOT_ORDER
                } else { // В случае если кол-во сверхпоставки < кол-ва категории "Незаказ", то
                    return PROCESSING_MERCURY_OVERDELIVERY_LESS_NOT_ORDER
                }
            } else {
                return PROCESSING_MERCURY_UNKNOWN
            }
        } else {
            return PROCESSING_MERCURY_UNKNOWN
        }
    }

    fun overDeliveryMoreEqualNotOrder(count: String, isConvertUnit: Boolean, typeDiscrepancies: String, manufacturer: String, productionDate: String) {
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(if (typeDiscrepancies != "41") count.toDouble() else 0.0)
        val delta = productCategoriesSum - productInfo.orderQuantity.toDouble()
        val enteredCount = count.toDouble() - delta

        // 1. Категорию "Незаказ" удаляем:
        // в текущей ВСД
        newVetProductDiscrepancies.filter {newVet ->
            newVet.typeDiscrepancies == "41" && newVet.manufacturer == manufacturer && newVet.productionDate == productionDate
        }.map {
            newVetProductDiscrepancies.remove(it)
        }
        //у продукта
        newProductDiscrepancies.findLast {it.typeDiscrepancies == "41"}?.let {
            newProductDiscrepancies.remove(it)
        }

        // 2. Кол-во сверхпоставки записываем в категорию "Норма"
        add(delta.toString(), isConvertUnit, "1", manufacturer, productionDate)

        // 3. Сохраняем выбранную категорию
        add(enteredCount.toString(), isConvertUnit, typeDiscrepancies, manufacturer, productionDate)
    }

    fun overDeliveryLessNotOrder(count: String, isConvertUnit: Boolean, typeDiscrepancies: String, manufacturer: String, productionDate: String) {
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(if (typeDiscrepancies != "41") count.toDouble() else 0.0)
        val delta = productCategoriesSum - productInfo.orderQuantity.toDouble()
        val enteredCount = count.toDouble() - delta

        // 1. Кол-во категории "Незаказ" уменьшаем в размере кол-ва сверхпоставки:
        // в текущей ВСД запоминаем, сколько было в категории "Незаказ" (у продукта столько же)
        val countNotOrderVetDoc = newVetProductDiscrepancies.filter {
            it.typeDiscrepancies == "41" && it.manufacturer == manufacturer && it.productionDate == productionDate
        }.sumByDouble {
            it.numberDiscrepancies
        } //кол-во расхождений по "Незаказ" считаем через фильтр, т.к. ВСД могут быть одинаковые по производителю и дате (см. vetDocumentVolume в ф-ции add() )
        //у текущей ВСД удаляем весь "Незаказ"
        newVetProductDiscrepancies.filter {newVet ->
            newVet.typeDiscrepancies == "41" && newVet.manufacturer == manufacturer && newVet.productionDate == productionDate
        }.map {
            newVetProductDiscrepancies.remove(it)
        }
        //у продукта удаляем весь "Незаказ"
        newProductDiscrepancies.findLast {it.typeDiscrepancies == "41"}?.let {
            newProductDiscrepancies.remove(it)
        }
        //записываем новое значение (из того, что было, вычитаем дельту)
        add((countNotOrderVetDoc - delta).toString(), isConvertUnit, "1", manufacturer, productionDate)

        // 2. Кол-во сверхпоставки записываем в категорию "Норма"
        add(delta.toString(), isConvertUnit, "1", manufacturer, productionDate)

        // 3. Сохраняем выбранную категорию
        add(enteredCount.toString(), isConvertUnit, typeDiscrepancies, manufacturer, productionDate)
    }

    private fun getQuantityAllCategoryExceptNonOrderOfVetDoc(count: Double, manufacturer: String, productionDate: String) : Double {
        return (newVetProductDiscrepancies.filter {newMercuryDiscrepancies ->
            newMercuryDiscrepancies.typeDiscrepancies != "41" && newMercuryDiscrepancies.manufacturer == manufacturer && newMercuryDiscrepancies.productionDate == productionDate
        }.sumByDouble {
            it.numberDiscrepancies
        }) + count
    }

    private fun getQuantityAllCategoryExceptNonOrderOfProduct(count: Double) : Double {
        return (newProductDiscrepancies.filter {newDiscrepancies ->
            newDiscrepancies.typeDiscrepancies != "41"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        } ) + count
    }

    private fun getQuantityNonOrderOfProduct(count: Double) : Double {
        return (newProductDiscrepancies.findLast {newDiscrepancies ->
            newDiscrepancies.typeDiscrepancies == "41"
        }?.numberDiscrepancies?.toDouble()) ?: 0.0 + count
    }

    fun checkConditionsOfPreservationPGE(count: Double, isConvertUnit: Boolean, reasonRejectionCode: String, manufacturer: String, productionDate: String) : Int {

        val vetDocumentIDVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        if ( (reasonRejectionCode == "2" && newProductDiscrepancies.any { it.typeDiscrepancies == "3" }) || (reasonRejectionCode == "3" && newProductDiscrepancies.any { it.typeDiscrepancies == "2"}) ) {
            return PROCESSING_MERCURY_UNDERLOAD_AND_SURPLUS_IN_ONE_DELIVERY
        }

        if (reasonRejectionCode == "2" &&
                getQuantityAllCategoryExceptNonOrderOfProductPGE(count) > productInfo.orderQuantity.toDouble() /**&& ddi скорее всего здесь должно быть два условия, больше, и меньше или равно, а значит условие не актуально, см. ТП ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) - 2.2.	Излишек по количеству
                (getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count, manufacturer, productionDate) > vetDocumentIDVolume || getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count, manufacturer, productionDate) < vetDocumentIDVolume)*/
        ) {
            return PROCESSING_MERCURY_SURPLUS_IN_QUANTITY
        }

        //норма и недогруз ТП 4.2
        if (reasonRejectionCode == "1") {
            //ТП 4.2.1.1
            if (newVetProductDiscrepancies.any {it.manufacturer == manufacturer &&
                            it.productionDate == productionDate &&
                            it.typeDiscrepancies == "3"} &&
                            getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) <= vetDocumentIDVolume &&
                            getQuantityAllCategoryExceptNonOrderOfProductPGE(count) <= productInfo.orderQuantity.toDouble()) {
                return PROCESSING_MERCURY_SAVED
            }
            if (newVetProductDiscrepancies.any {it.manufacturer == manufacturer &&
                            it.productionDate == productionDate &&
                            it.typeDiscrepancies == "3"} &&
                    getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) <= vetDocumentIDVolume &&
                    getQuantityAllCategoryExceptNonOrderOfProductPGE(count) > productInfo.orderQuantity.toDouble()) {
                return PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_INVOICE
            }
            //ТП 4.2.1.2
            if (newVetProductDiscrepancies.any {it.manufacturer == manufacturer &&
                            it.productionDate == productionDate &&
                            it.typeDiscrepancies == "3"} &&
                            getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) > vetDocumentIDVolume) {
                return PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_VET_DOC
            }
        }

        if ( getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) > vetDocumentIDVolume ) {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC
        }

        return if (getQuantityAllCategoryExceptNonOrderOfProductPGE(count) <= productInfo.orderQuantity.toDouble()) {
            PROCESSING_MERCURY_SAVED
        } else {
            PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }
    }

    private fun getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count: Double, manufacturer: String, productionDate: String) : Double {
        return (newVetProductDiscrepancies.filter {newMercuryDiscrepancies ->
            newMercuryDiscrepancies.manufacturer == manufacturer && newMercuryDiscrepancies.productionDate == productionDate
        }.sumByDouble {
            it.numberDiscrepancies
        }) + count
    }

    private fun getQuantityAllCategoryExceptNonOrderOfProductPGE(count: Double) : Double {
        return (newProductDiscrepancies.sumByDouble {
            it.numberDiscrepancies.toDouble()
        } ) + count
    }

    fun getGoodsDetails() : List<TaskProductDiscrepancies>? {
        return newProductDiscrepancies
    }

    fun getNewCountAccept() : Double {
        return newProductDiscrepancies.filter {
            it.typeDiscrepancies == "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getNewCountAcceptPGE() : Double {
        return newProductDiscrepancies.filter {
            it.typeDiscrepancies == "1" || it.typeDiscrepancies == "2"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getNewCountRefusal() : Double {
        return newProductDiscrepancies.filter {
            it.typeDiscrepancies != "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getNewCountRefusalPGE() : Double {
        return newProductDiscrepancies.filter {
            it.typeDiscrepancies == "3" || it.typeDiscrepancies == "4" || it.typeDiscrepancies == "5"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    private fun getNewCountByDiscrepanciesOfProduct(typeDiscrepancies: String) : Double {
        return newProductDiscrepancies.filter {
            it.typeDiscrepancies == typeDiscrepancies
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun modifications() : Boolean {
        return isModifications
    }

    //функции для конвертации единиц измерения, если по продукту и ВСД они не совпадают
    private fun convertUnitForPGEVsd(countValue: Double) : Double {
        return countValue / productInfo.quantityInvest.toDouble()
    }
    private fun convertUnitForPGEProduct(countValue: Double) : Double {
        return countValue * productInfo.quantityInvest.toDouble()
    }

    //В случае, если пользователь согласился округлить, то фактическое значение приравнивается к плановому.
    fun getRoundingQuantityPPP (count: String, reasonRejectionCode: String) : Double {
        val residue = productInfo.origQuantity.toDouble() - getQuantityAllCategoryExceptNonOrderOfProduct(if (reasonRejectionCode != "41") count.toDouble() else 0.0)
        return count.toDouble() + residue
    }

}