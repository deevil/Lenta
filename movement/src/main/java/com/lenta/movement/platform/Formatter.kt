package com.lenta.movement.platform

import com.lenta.movement.models.*
import com.lenta.shared.models.core.Uom

class Formatter : IFormatter {

    override fun formatMarketName(market: String): String {
        return "$TK - $market"
    }

    override fun getProductName(product: ProductInfo): String {
        return "${product.getMaterialLastSix()} ${product.description}"
    }

    override fun getTaskTypeNameDescription(taskType: TaskType): String {
        return when (taskType) {
            TaskType.TransferWithOrder -> TRANSFER_WITH_ORDER
            TaskType.TransferWithoutOrder -> TRANSFER_WITHOUT_ORDER
        }
    }

    override fun getTaskStatusName(taskStatus: Task.Status): String {
        return taskStatus.text ?: when (taskStatus) {
            is Task.Status.Created -> Task.Status.CREATED
            is Task.Status.Counted -> Task.Status.COUNTED
            is Task.Status.Published -> Task.Status.PUBLISHED
            is Task.Status.Unknown -> Task.Status.UNKNOWN
            is Task.Status.ToConsolidation -> Task.Status.TO_CONSOLIDATION
            is Task.Status.Consolidation -> Task.Status.CONSOLIDATION
            is Task.Status.Consolidated -> Task.Status.CONSOLIDATED
            is Task.Status.ProcessingOnGz -> Task.Status.PROCESSING_ON_GZ
        }
    }

    override fun getBasketName(basket: Basket): String {
        return "$BASKET ${String.format("%02d", basket.number)}"
    }

    /** <Тип ГИС-контроля>/<Вид товара>/ПП – <номер задания>/ С-<номер секции>/<Группа закупок>/
     * <Марочный или партионный>/<Номер партии>/<Еда/не еда>/<SAP-код товара>>. */
    override fun getBasketDescription(basket: Basket, task: Task, settings: TaskSettings): String {
        if (basket.keys.isEmpty()) return ""

        return buildString {
            val signsOfDiv = settings.signsOfDiv

            /** <Тип ГИС-контроля> - если в справочнике установлен признак «DIV_ALCO»,
             * то отображать <А> для алкогольной корзины, если установлен признак «DIV_VET»,
             * то отображать <В> для ветеринарных товаров и <О> для обычных товаров, если
             * установлен признак «DIV_USUAL». */
            if (signsOfDiv.contains(GoodsSignOfDivision.ALCO) && basket.keys.first().isAlco) {
                append(A_AND_SLASH)
            } else if (signsOfDiv.contains(GoodsSignOfDivision.VET) && basket.keys.first().isVet) {
                append(B_AND_SLASH)
            } else if (signsOfDiv.contains(GoodsSignOfDivision.USUAL) && basket.keys.first().isUsual) {
                append(O_AND_SLASH)
            }
            //<Вид товара>
            if (signsOfDiv.contains(GoodsSignOfDivision.MTART)) {
                append("${basket.keys.first().materialType}/")
            }
            //ПП – <номер поставщика>
            if (signsOfDiv.contains(GoodsSignOfDivision.LIF_NUMBER)) {
                val supplier = basket.keys.firstOrNull()?.suppliers?.firstOrNull()
                supplier?.code?.takeIf { it.isNotEmpty() }?.let { supplierCode ->
                    append("$PP - ${supplierCode}/")
                }
            }

            //С-<номер секции>
            if (signsOfDiv.contains(GoodsSignOfDivision.SECTION)) {
                append("$SECTION_CHAR - ${basket.keys.first().sectionId}/")
            }

            /**<Марочный или партионный> - отображать как <М> (марочный, есть признак IS_EXC)
             * или <П> (партионный, нет признака IS_EXC)*/
            if (signsOfDiv.contains(GoodsSignOfDivision.MARK_PARTS)) {
                val firstKey = basket.keys.first()
                val splashToAppend = when {
                    firstKey.isExcise -> M_AND_SLASH
                    firstKey.isNotExcise -> P_AND_SLASH
                    else -> ""
                }
                append(splashToAppend)
            }

           //<Номер партии>
//            if (signsOfDiv.contains(GoodsSignOfDivision.PARTS)) {
//                append(basket.keys.firstOrNull()?.batchNumber) TODO добавить когда появится деление товаров по категориям
//            }

            if (signsOfDiv.contains(GoodsSignOfDivision.FOOD)) {
                val charToAppend = if (basket.keys.first().isFood) BASKET_DESC_FOOD_CHAR_AND_SLASH
                else BASKET_DESC_NOT_FOOD_CHAR_AND_SLASH
                append(charToAppend)
            }

            if (signsOfDiv.contains(GoodsSignOfDivision.MATERIAL_NUMBER)) {
                append(basket.keys.first().getMaterialLastSix())
            }
        }
    }

    override fun basketGisControl(basket: Basket): String {
        return when {
            basket.isAlco ?: false -> ALCO
            basket.isExciseAlco ?: false -> EXCISE_ALCO
            basket.isNotExciseAlco ?: false -> NOT_EXCISE_ALCO
            basket.isUsual ?: false -> USUAL
            basket.isVet ?: false -> VET
            basket.isFood ?: false -> FOOD
            else -> ""
        }
    }

    override fun getEOSubtitle(eo: ProcessingUnit): String {
        val geNumber = eo.cargoUnitNumber
        return if (geNumber == null) {
            getEOSubtitleForInsides(eo)
        } else {
            "$GE-$geNumber"
        }
    }

    override fun getEOSubtitleForInsides(eo: ProcessingUnit): String {
        val isAlco = eo.isAlco ?: false
        val isUsual = eo.isUsual ?: false
        return buildString {
            append("№${eo.basketNumber}")
            append(
                    when {
                        isAlco -> SLASH_AND_ALCO_CHAR
                        isUsual -> SLASH_AND_USUAL_CHAR
                        else -> ""
                    }
            )
            append(eo.supplier.orEmpty())
        }
    }

    override fun getGETitle(ge: CargoUnit): String {
        val eoList = ge.eoList
        return if (ge.eoList.size == 1) {
            eoList[0].processingUnitNumber
        } else {
            ge.number
        }
    }

    override fun getOrderUnitsNameByCode(orderUnits: String): String =
            when (orderUnits) {
                Uom.DEFAULT.code -> Uom.DEFAULT.name
                Uom.G.code -> Uom.G.name
                Uom.KAR.code -> Uom.KAR.name
                Uom.KG.code -> Uom.KG.name
                Uom.ST.code -> Uom.KG.name
                else -> "Wrong uom code"
            }

    override fun getTaskTitle(task: Task): String {
        return with(task) {
            val taskTypeShortName = taskType.shortName
            "$taskTypeShortName-$number // $name"
        }
    }

    override fun getBasketTitle(basket: Basket, task: Task, taskSettings: TaskSettings) = buildString {
        val basketName = getBasketName(basket)
        val basketDescription = getBasketDescription(
                basket = basket,
                task = task,
                settings = taskSettings
        )
        append(basketName)
        append(": ")
        append(basketDescription)
    }

    companion object {
        private const val A_AND_SLASH = "А/"
        private const val B_AND_SLASH = "В/"
        private const val O_AND_SLASH = "О/"
        private const val M_AND_SLASH = "М/"
        private const val P_AND_SLASH = "П/"

        private const val TK = "ТК"
        private const val GE = "ГЕ"
        private const val PP = "ПП"

        private const val BASKET = "Корзина"
        private const val BASKET_DESC_FOOD_CHAR_AND_SLASH = "F/"
        private const val BASKET_DESC_NOT_FOOD_CHAR_AND_SLASH = "NF/"

        private const val ALCO = "Алкоголь"
        private const val SLASH_AND_ALCO_CHAR = "/А"
        private const val EXCISE_ALCO = "Марочные остатки"
        private const val NOT_EXCISE_ALCO = "Партионные остатки"
        private const val USUAL = "Обычный товар"
        private const val SLASH_AND_USUAL_CHAR = "/О"
        private const val VET = "Меркурианский товар"
        private const val FOOD = "Еда"

        private const val SECTION_CHAR = "С"

        private const val TRANSFER_WITH_ORDER = "Трансфер с заказа"
        private const val TRANSFER_WITHOUT_ORDER = "Трансфер без заказа"
    }
}