package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate
import javax.inject.Inject

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
    private val currentProductDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private val currentMercuryDiscrepancies: ArrayList<TaskMercuryDiscrepancies> = ArrayList()
    private var isModifications: Boolean = false

    fun newProcessMercuryProductService(productInfo: TaskProductInfo) : ProcessMercuryProductService? {
        return if (productInfo.isVet){
            this.productInfo = productInfo.copy()
            val taskRepository = taskManager.getReceivingTask()?.taskRepository
            currentProductDiscrepancies.clear()
            taskRepository
                    ?.getProductsDiscrepancies()
                    ?.findProductDiscrepanciesOfProduct(productInfo)
                    ?.mapTo(currentProductDiscrepancies) { it.copy() }
            currentMercuryDiscrepancies.clear()
            taskRepository
                    ?.getMercuryDiscrepancies()
                    ?.findMercuryDiscrepanciesOfProduct(productInfo)
                    ?.mapTo(currentMercuryDiscrepancies) { it.copy() }
            isModifications = false
            this
        }
        else null
    }

    fun add(count: String, isConvertUnit: Boolean, typeDiscrepancies: String, manufacturer: String, productionDate: String){
        isModifications = true
        val countAdd = getCountByDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()

        //добавляем кол-во по расхождению для продукта
        var foundDiscrepancy = currentProductDiscrepancies.findLast {
            it.typeDiscrepancies == typeDiscrepancies
        }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(
                                numberDiscrepancies = countAdd.toString(),
                                processingUnitNumber = productInfo.processingUnit
                        )
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

        changeProductDiscrepancy(foundDiscrepancy)

        //добавляем кол-во по расхождению для ВСД
        var countAllAddVetDoc = if (isConvertUnit) convertUnitForPGEVsd(countAdd) else countAdd

        val foundVetDiscrepancy =
                currentMercuryDiscrepancies
                        .filter { filterMercury ->
                            filterMercury.manufacturer == manufacturer
                                    && filterMercury.productionDate == productionDate
                        }
                        .mapNotNull { mapMercury ->
                            val remainderOfVsdAndDiscrepancy = getRemainderOfVsdAndDiscrepancies(mapMercury.vetDocumentID, typeDiscrepancies, mapMercury.volume)
                            val addCount: Double
                            countAllAddVetDoc
                                    .takeIf {
                                        it > 0.0
                                    }
                                    ?.run {
                                        if (remainderOfVsdAndDiscrepancy <= this) {
                                            addCount = remainderOfVsdAndDiscrepancy
                                            countAllAddVetDoc -= remainderOfVsdAndDiscrepancy
                                        } else {
                                            addCount = countAllAddVetDoc
                                            countAllAddVetDoc = 0.0
                                        }

                                        TaskMercuryDiscrepancies(
                                                materialNumber = mapMercury.materialNumber,
                                                vetDocumentID = mapMercury.vetDocumentID,
                                                volume = mapMercury.volume,
                                                uom = productInfo.uom,
                                                typeDiscrepancies = typeDiscrepancies,
                                                numberDiscrepancies = addCount,
                                                productionDate = mapMercury.productionDate,
                                                manufacturer = mapMercury.manufacturer,
                                                productionDateTo = mapMercury.productionDateTo
                                        )
                                    }
                        }

        foundVetDiscrepancy.map { mercury ->
            changeVetProductDiscrepancy(mercury)
        }
    }

    fun addSurplusInQuantityPGE(count: Double, isConvertUnit: Boolean, manufacturer: String, productionDate: String) {
        val countNormAdd = productInfo.orderQuantity.toDouble() - (getQuantityAllCategoryExceptNonOrderOfProductPGE(count) - count)
        val countSurplusAdd = count - countNormAdd
        if (countNormAdd > 0.0) {
            add(
                    count = countNormAdd.toString(),
                    isConvertUnit = isConvertUnit,
                    typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM,
                    manufacturer = manufacturer,
                    productionDate = productionDate
            )
        }
        if (countSurplusAdd > 0.0) {
            add(
                    count = countSurplusAdd.toString(),
                    isConvertUnit = isConvertUnit,
                    typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS,
                    manufacturer = manufacturer,
                    productionDate = productionDate
            )
        }
    }

    fun addNormAndUnderloadExceededInvoicePGE(count: Double, isConvertUnit: Boolean, manufacturer: String, productionDate: String) {
        val countExceeded = getQuantityAllCategoryExceptNonOrderOfProductPGE(count) - productInfo.orderQuantity.toDouble()

        //удаляем превышающее количество из недогруза в текущей ВСД
        var delCount: Double = if (isConvertUnit) convertUnitForPGEVsd(countExceeded) else countExceeded
        filteredCurrentMercuryDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_UNDERLOAD, manufacturer, productionDate)
                .forEach {
                    delCount -= if ((it.numberDiscrepancies - delCount) < 0.0) {
                        currentMercuryDiscrepancies.remove(it)
                        it.numberDiscrepancies
                    } else {
                        if ((it.numberDiscrepancies - delCount) > 0.0 ) {
                            changeVetProductDiscrepancy(it.copy(numberDiscrepancies = it.numberDiscrepancies - delCount))
                        } else {
                            currentMercuryDiscrepancies.remove(it)
                        }
                        it.numberDiscrepancies
                    }
                }

        //у продукта удаляем превышающее количество из недогруза
        currentProductDiscrepancies.findLast {it.typeDiscrepancies == "3"}?.let {
            if ((it.numberDiscrepancies.toDouble() - countExceeded) > 0.0) {
                changeProductDiscrepancy(it.copy(numberDiscrepancies = (it.numberDiscrepancies.toDouble() - countExceeded).toString() ))
            } else {
                currentProductDiscrepancies.remove(it)
            }
        }

        //добавляем в Норму превышающее количество из недогруза кол-во до максимума по инфойсу(productInfo.orderQuantity)
        add(count.toString(), isConvertUnit, "1", manufacturer, productionDate)
    }

    fun addNormAndUnderloadExceededVetDocPGE(count: Double, isConvertUnit: Boolean, manufacturer: String, productionDate: String) {
        val vetDocumentVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryDiscrepanciesOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        val countExceeded = getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) - vetDocumentVolume
        val countUnderloadVetDoc =
                filteredCurrentMercuryDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_UNDERLOAD, manufacturer, productionDate)
                        .sumByDouble {
                            it.numberDiscrepancies
                        } //кол-во расхождений по недогрузу считаем через фильтр, т.к. ВСД могут быть одинаковые по производителю и дате (см. vetDocumentVolume в ф-ции add() )

        //удаляем расхождения по недогрузу из текущей ВСД
        currentMercuryDiscrepancies.removeAll(filteredCurrentMercuryDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_UNDERLOAD, manufacturer, productionDate))

        //у продукта удаляем кол-во недогруза указанного в текущей ВСД
        val countUnderloadProduct = if (isConvertUnit) convertUnitForPGEProduct(countUnderloadVetDoc) else countUnderloadVetDoc
        currentProductDiscrepancies.findLast {it.typeDiscrepancies == "3"}?.let {
            if ((it.numberDiscrepancies.toDouble() - countUnderloadProduct) > 0.0) {
                changeProductDiscrepancy(it.copy(numberDiscrepancies = (it.numberDiscrepancies.toDouble() - countUnderloadProduct).toString() ))
            } else {
                currentProductDiscrepancies.remove(it)
            }
        }

        //после удаления недогруза подсчитываем оставшееся кол-во по текущей ВСД
        val vetDocumentCountAlreadyAdded = currentMercuryDiscrepancies.filter {
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

    fun deleteDetails(typeDiscrepancies: String) {
        currentProductDiscrepancies.removeItemFromListWithPredicate {
            it.typeDiscrepancies == typeDiscrepancies
                    && !it.isNotEdit
        }

        currentMercuryDiscrepancies.removeItemFromListWithPredicate {
            it.typeDiscrepancies == typeDiscrepancies
        }

        taskManager
                .getReceivingTask()
                ?.let { task ->
                    task.taskRepository
                            .getMercuryDiscrepancies()
                            .findMercuryDiscrepanciesOfProduct(productInfo)
                            .mapTo(currentMercuryDiscrepancies) { it.copy() }
                }
    }

    fun save(){
        if (currentProductDiscrepancies.isNotEmpty()) {
            currentProductDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getProductsDiscrepancies()?.
                        changeProductDiscrepancyOfProcessingUnit(it)
            }
        }

        if (currentMercuryDiscrepancies.isNotEmpty()) {
            currentMercuryDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getMercuryDiscrepancies()?.
                        changeMercuryDiscrepancy(it)
            }
        }
    }

    private fun changeProductDiscrepancy(newDiscrepancy: TaskProductDiscrepancies) {
        var index = -1
        for (i in currentProductDiscrepancies.indices) {
            if (newDiscrepancy.typeDiscrepancies == currentProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index != -1) {
            currentProductDiscrepancies.removeAt(index)
        }

        currentProductDiscrepancies.add(newDiscrepancy)
    }

    private fun changeVetProductDiscrepancy(newVetDiscrepancy: TaskMercuryDiscrepancies) {
        var index = -1
        for (i in currentMercuryDiscrepancies.indices) {
            if (newVetDiscrepancy.vetDocumentID == currentMercuryDiscrepancies[i].vetDocumentID
                    && (newVetDiscrepancy.typeDiscrepancies == currentMercuryDiscrepancies[i].typeDiscrepancies
                            || currentMercuryDiscrepancies[i].typeDiscrepancies.isEmpty())) {
                index = i
            }
        }

        if (index != -1) {
            currentMercuryDiscrepancies.removeAt(index)
        }

        currentMercuryDiscrepancies.add(newVetDiscrepancy)
    }

    fun checkConditionsOfPreservationOfProduct(count: String,
                                               typeDiscrepancies: String,
                                               manufacturer: String,
                                               productionDate: String,
                                               paramGrzRoundLackRatio: Double,
                                               paramGrzRoundLackUnit: Double,
                                               paramGrzRoundHeapRatio: Double) : Int {

        //суммарное кол-во по ВСД (ET_VET_DIFF, поле VSDVOLUME)
        val vetDocumentIDVolume = getVolumeAllMercury(manufacturer, productionDate)

        //1.1. Проверяем есть ли разница между плановым кол-ом в ВП и суммой фактически введенного кол-ва с учетом всех введенных категорий, за исключением категории “Незаказ”
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(if (typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER) count.toDouble() else 0.0)
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
        val vetDocumentIDVolume = getVolumeAllMercury(manufacturer, productionDate)

        //Сумма всех заявленных категорий по текущему ВСД за исключением «Незаказ»
        val batchCategoriesSum = getQuantityAllCategoryExceptNonOrderOfVetDoc(if (typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER) count.toDouble() else 0.0, manufacturer, productionDate)
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
        val notOrderCount = getQuantityNonOrderOfProduct(if (typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER) count.toDouble() else 0.0)
        // 3. Сумма всех заявленных категорий по текущему ВСД за исключением «Незаказ» <= количеству в ВП за вычитом категории незаказ(ET_TASK_POS, поле MENGE)
        if (batchCategoriesSum <= productInfo.origQuantity.toDouble() - notOrderCount) {
            //Если условие выполнено - сохранять результат пересчета.
            return PROCESSING_MERCURY_SAVED
        }

        //В случае если кол-во в поставке меньше кол-ва в заказе, то при вводе кол-ва больше чем в ВП выводим ошибку о превышении кол-ва в ВП.
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(if (typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER) count.toDouble() else 0.0)
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
                val notOrderedCategory = currentProductDiscrepancies.first { it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER}

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
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(if (typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER) count.toDouble() else 0.0)
        val delta = productCategoriesSum - productInfo.orderQuantity.toDouble()
        val enteredCount = count.toDouble() - delta

        // 1. Категорию "Незаказ" удаляем:
        // в текущей ВСД
        filteredCurrentMercuryDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER, manufacturer, productionDate)
                .forEach {
                    currentMercuryDiscrepancies.remove(it)
                }
        //у продукта
        currentProductDiscrepancies
                .findLast {it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER}
                ?.let {
                    currentProductDiscrepancies.remove(it)
                }

        // 2. Кол-во сверхпоставки записываем в категорию "Норма"
        add(delta.toString(), isConvertUnit, TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM, manufacturer, productionDate)

        // 3. Сохраняем выбранную категорию
        add(enteredCount.toString(), isConvertUnit, typeDiscrepancies, manufacturer, productionDate)
    }

    fun overDeliveryLessNotOrder(count: String, isConvertUnit: Boolean, typeDiscrepancies: String, manufacturer: String, productionDate: String) {
        val addCount = if (typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER) count.toDouble() else 0.0
        val productCategoriesSum = getQuantityAllCategoryExceptNonOrderOfProduct(addCount)
        val delta = productCategoriesSum - productInfo.orderQuantity.toDouble()
        val enteredCount = count.toDouble() - delta

        // 1. Кол-во категории "Незаказ" уменьшаем в размере кол-ва сверхпоставки:
        // в текущей ВСД запоминаем, сколько было в категории "Незаказ" (у продукта столько же)
        //кол-во расхождений по "Незаказ" считаем через фильтр, т.к. ВСД могут быть одинаковые по производителю и дате (см. vetDocumentVolume в ф-ции add() )
        val countNotOrderVetDoc =
                filteredCurrentMercuryDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER, manufacturer, productionDate)
                        .sumByDouble {
                            it.numberDiscrepancies
                        }
        //у текущей ВСД удаляем весь "Незаказ"
        filteredCurrentMercuryDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER, manufacturer, productionDate)
                .forEach {
                    currentMercuryDiscrepancies.remove(it)
                }
        //у продукта удаляем весь "Незаказ"
        currentProductDiscrepancies
                .findLast {it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER}
                ?.let {
                    currentProductDiscrepancies.remove(it)
                }
        //записываем новое значение (из того, что было, вычитаем дельту)
        add((countNotOrderVetDoc - delta).toString(), isConvertUnit, TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM, manufacturer, productionDate)

        // 2. Кол-во сверхпоставки записываем в категорию "Норма"
        add(delta.toString(), isConvertUnit, TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM, manufacturer, productionDate)

        // 3. Сохраняем выбранную категорию
        add(enteredCount.toString(), isConvertUnit, typeDiscrepancies, manufacturer, productionDate)
    }

    private fun getQuantityAllCategoryExceptNonOrderOfVetDoc(count: Double, manufacturer: String, productionDate: String) : Double {
        val countNotOrder =
                filteredCurrentMercuryDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER, manufacturer, productionDate)
                        .sumByDouble { it.numberDiscrepancies }
        return countNotOrder + count
    }

    private fun getQuantityAllCategoryExceptNonOrderOfProduct(count: Double) : Double {
        val countExceptNotOrder =
                currentProductDiscrepancies
                        .filter { newDiscrepancies ->
                            newDiscrepancies.typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER
                        }
                        .sumByDouble { it.numberDiscrepancies.toDouble() }
        return countExceptNotOrder + count
    }

    private fun getQuantityNonOrderOfProduct(count: Double) : Double {
        val countNotOrder =
                currentProductDiscrepancies
                        .findLast { it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER }
                        ?.numberDiscrepancies
                        ?.toDouble()
                        ?: 0.0
        return countNotOrder + count
    }

    fun checkConditionsOfPreservationPGE(count: Double, isConvertUnit: Boolean, reasonRejectionCode: String, manufacturer: String, productionDate: String) : Int {

        val vetDocumentIDVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryDiscrepanciesOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        if ( (reasonRejectionCode == "2" && currentProductDiscrepancies.any { it.typeDiscrepancies == "3" }) || (reasonRejectionCode == "3" && currentProductDiscrepancies.any { it.typeDiscrepancies == "2"}) ) {
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
            if (currentMercuryDiscrepancies.any {it.manufacturer == manufacturer &&
                            it.productionDate == productionDate &&
                            it.typeDiscrepancies == "3"} &&
                            getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) <= vetDocumentIDVolume &&
                            getQuantityAllCategoryExceptNonOrderOfProductPGE(count) <= productInfo.orderQuantity.toDouble()) {
                return PROCESSING_MERCURY_SAVED
            }
            if (currentMercuryDiscrepancies.any {it.manufacturer == manufacturer &&
                            it.productionDate == productionDate &&
                            it.typeDiscrepancies == "3"} &&
                    getQuantityAllCategoryExceptNonOrderOfVetDocPGE(if (isConvertUnit) convertUnitForPGEVsd(count) else count, manufacturer, productionDate) <= vetDocumentIDVolume &&
                    getQuantityAllCategoryExceptNonOrderOfProductPGE(count) > productInfo.orderQuantity.toDouble()) {
                return PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_INVOICE
            }
            //ТП 4.2.1.2
            if (currentMercuryDiscrepancies.any {it.manufacturer == manufacturer &&
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
        return (currentMercuryDiscrepancies.filter {newMercuryDiscrepancies ->
            newMercuryDiscrepancies.manufacturer == manufacturer && newMercuryDiscrepancies.productionDate == productionDate
        }.sumByDouble {
            it.numberDiscrepancies
        }) + count
    }

    private fun getQuantityAllCategoryExceptNonOrderOfProductPGE(count: Double) : Double {
        return (currentProductDiscrepancies.sumByDouble {
            it.numberDiscrepancies.toDouble()
        } ) + count
    }

    fun getGoodsDetails() : List<TaskProductDiscrepancies>? {
        return currentProductDiscrepancies
    }

    fun getCountAccept() : Double {
        return currentProductDiscrepancies.filter {
            it.typeDiscrepancies == "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountAcceptPGE() : Double {
        return currentProductDiscrepancies.filter {
            it.typeDiscrepancies == "1" || it.typeDiscrepancies == "2"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountRefusal() : Double {
        return currentProductDiscrepancies.filter {
            it.typeDiscrepancies != "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountRefusalPGE() : Double {
        return currentProductDiscrepancies.filter {
            it.typeDiscrepancies == "3" || it.typeDiscrepancies == "4" || it.typeDiscrepancies == "5"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    private fun getCountByDiscrepanciesOfProduct(typeDiscrepancies: String) : Double {
        return currentProductDiscrepancies.filter {
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
        val addCount = if (reasonRejectionCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER) count.toDouble() else 0.0
        val residue = productInfo.origQuantity.toDouble() - getQuantityAllCategoryExceptNonOrderOfProduct(addCount)
        return count.toDouble() + residue
    }

    fun getVolumeAllMercury(manufacturer: String, productionDate: String) : Double {
        return currentMercuryDiscrepancies
                .asSequence()
                .filter {
                    it.manufacturer == manufacturer
                            && it.productionDate == productionDate
                }
                .groupBy {
                    it.vetDocumentID
                }
                .map { groupByMercuryDiscrepancies ->
                    groupByMercuryDiscrepancies
                            .value
                            .first()
                            .volume
                }.sumByDouble {
                    it
                }
    }

    fun getUomNameOfMercury(manufacturer: String, productionDate: String) : String {
        return currentMercuryDiscrepancies
                .findLast {
                    it.manufacturer == manufacturer
                            && it.productionDate == productionDate
                }
                ?.uom
                ?.name
                .orEmpty()
    }

    private fun getRemainderOfVsdAndDiscrepancies(vetDocumentID: String, typeDiscrepancies: String, mercuryVolume: Double) : Double {
        val allNumberDiscrepanciesByVsd =
                currentMercuryDiscrepancies
                        .filter { it.vetDocumentID == vetDocumentID }
                        .sumByDouble { it.numberDiscrepancies }
        val remainderOfVsd = mercuryVolume - allNumberDiscrepanciesByVsd
        val numberDiscrepanciesByDiscrepancies =
                currentMercuryDiscrepancies
                        .filter {
                            it.vetDocumentID == vetDocumentID
                                    && it.typeDiscrepancies == typeDiscrepancies
                        }
                        .sumByDouble { it.numberDiscrepancies }
        return  remainderOfVsd + numberDiscrepanciesByDiscrepancies
    }

    private fun filteredCurrentMercuryDiscrepancies(typeDiscrepancies: String, manufacturer: String, productionDate: String) : List<TaskMercuryDiscrepancies> {
        return currentMercuryDiscrepancies.filter {
            it.typeDiscrepancies == typeDiscrepancies && it.manufacturer == manufacturer && it.productionDate == productionDate
        }
    }

}