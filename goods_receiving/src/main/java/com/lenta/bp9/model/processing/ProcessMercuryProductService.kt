package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import javax.inject.Inject

const val PROCESSING_MERCURY_SAVED = 1
const val PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC = 2
const val PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE = 3
const val PROCESSING_MERCURY_SURPLUS_IN_QUANTITY = 4
const val PROCESSING_MERCURY_UNDERLOAD_AND_SURPLUS_IN_ONE_DELIVERY = 5
const val PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_INVOICE = 6
const val PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_VET_DOC = 7

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

    fun add(count: String, typeDiscrepancies: String, manufacturer: String, productionDate: String){
        isModifications = true
        val countAdd = getNewCountByDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()

        var foundDiscrepancy = newProductDiscrepancies.findLast {
            it.typeDiscrepancies == typeDiscrepancies
        }

        foundDiscrepancy = foundDiscrepancy?.copy(numberDiscrepancies = countAdd.toString())
                ?: TaskProductDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        exidv = "",
                        numberDiscrepancies = countAdd.toString(),
                        uom = productInfo.uom,
                        typeDiscrepancies = typeDiscrepancies,
                        isNotEdit = false,
                        isNew = false,
                        notEditNumberDiscrepancies = ""
                )

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
        var countAllAddVetDoc = countAdd + vetDocumentCountAlreadyAdded

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

        changeNewProductDiscrepancy(foundDiscrepancy)
        foundVetDiscrepancy?.map {
            changeNewVetProductDiscrepancy(it)
        }
    }

    fun addSurplusInQuantityPGE(count: Double, manufacturer: String, productionDate: String) {
        val countNormAdd = productInfo.orderQuantity.toDouble() - (getQuantityAllCategoryExceptNonOrderOfProductPGE(count) - count)
        val countSurplusAdd = count - countNormAdd
        if (countNormAdd > 0.0) {
            add(countNormAdd.toString(), "1", manufacturer, productionDate)
        }
        if (countSurplusAdd > 0.0) {
            add(countSurplusAdd.toString(), "2", manufacturer, productionDate)
        }
    }

    fun addNormAndUnderloadExceededInvoicePGE(count: Double, manufacturer: String, productionDate: String) {
        val countExceeded = getQuantityAllCategoryExceptNonOrderOfProductPGE(count) - productInfo.orderQuantity.toDouble()

        //удаляем превышающее количество из недогруза в текущей ВСД
        var delCount: Double = countExceeded
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
        add(count.toString(), "1", manufacturer, productionDate)
    }

    fun addNormAndUnderloadExceededVetDocPGE(count: Double, manufacturer: String, productionDate: String) {
        val vetDocumentVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        val countExceeded = getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count, manufacturer, productionDate) - vetDocumentVolume
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
        newProductDiscrepancies.findLast {it.typeDiscrepancies == "3"}?.let {
            if ((it.numberDiscrepancies.toDouble() - countUnderloadVetDoc) > 0.0) {
                changeNewProductDiscrepancy(it.copy(numberDiscrepancies = (it.numberDiscrepancies.toDouble() - countUnderloadVetDoc).toString() ))
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
        add(countNormAdd.toString(), "1", manufacturer, productionDate)
        add(countExceeded.toString(), "2", manufacturer, productionDate)
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

        //Кол-во, которое было оприходовано по этому заказу и этому товару
        val quantityCapitalized = ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0) +
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0)).toString()

        productInfo = productInfo.copy(quantityCapitalized =  quantityCapitalized)

        taskManager.getReceivingTask()?.
                taskRepository?.
                getProducts()?.
                changeProduct(productInfo.copy(quantityCapitalized = quantityCapitalized))


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

    fun checkConditionsOfPreservation(count: String, reasonRejectionCode: String, manufacturer: String, productionDate: String) : Int {

        val vetDocumentIDVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        if ( getQuantityAllCategoryExceptNonOrderOfVetDoc(if (reasonRejectionCode != "41") count.toDouble() else 0.0, manufacturer, productionDate) > vetDocumentIDVolume ) {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC
        }

        if ( getQuantityAllCategoryExceptNonOrderOfVetDoc(if (reasonRejectionCode != "41") count.toDouble() else 0.0, manufacturer, productionDate) <= productInfo.orderQuantity.toDouble() ) {
            return PROCESSING_MERCURY_SAVED
        } else {
            if (productInfo.uom.name == "ШТ") {
                return PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
            }
        }

        return if (getQuantityAllCategoryExceptNonOrderOfProduct(if (reasonRejectionCode != "41") count.toDouble() else 0.0) <= (productInfo.origQuantity.toDouble() + productInfo.overdToleranceLimit.toDouble())) {
            PROCESSING_MERCURY_SAVED
        } else {
            PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }
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

    fun checkConditionsOfPreservationPGE(count: Double, reasonRejectionCode: String, manufacturer: String, productionDate: String) : Int {

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
                getQuantityAllCategoryExceptNonOrderOfProductPGE(count) > productInfo.orderQuantity.toDouble() /**&& ddi скорее всего здесь должно быть два условия, больше, и меньше или равно, а згачит условие не актуально, см. ТП ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) - 2.2.	Излишек по количеству
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
                            getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count, manufacturer, productionDate) <= vetDocumentIDVolume &&
                            getQuantityAllCategoryExceptNonOrderOfProductPGE(count) <= productInfo.orderQuantity.toDouble()) {
                return PROCESSING_MERCURY_SAVED
            }
            if (newVetProductDiscrepancies.any {it.manufacturer == manufacturer &&
                            it.productionDate == productionDate &&
                            it.typeDiscrepancies == "3"} &&
                    getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count, manufacturer, productionDate) <= vetDocumentIDVolume &&
                    getQuantityAllCategoryExceptNonOrderOfProductPGE(count) > productInfo.orderQuantity.toDouble()) {
                return PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_INVOICE
            }
            //ТП 4.2.1.2
            if (newVetProductDiscrepancies.any {it.manufacturer == manufacturer &&
                            it.productionDate == productionDate &&
                            it.typeDiscrepancies == "3"} &&
                            getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count, manufacturer, productionDate) > vetDocumentIDVolume) {
                return PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_VET_DOC
            }
        }

        if ( getQuantityAllCategoryExceptNonOrderOfVetDocPGE(count, manufacturer, productionDate) > vetDocumentIDVolume ) {
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

}