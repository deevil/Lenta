package com.lenta.bp9.model.task

import com.lenta.shared.models.core.*

//ET_TASK_POS Таблица состава задания ППП
class TaskProductInfo(materialNumber: String,
                      description: String,
                      uom: Uom,
                      type: ProductType,
                      isSet: Boolean,
                      sectionId: String,
                      matrixType: MatrixType,
                      materialType: String,
                      val origQuantity: String, //Исходное количество позиции поставки
                      val orderQuantity: String, //Кол-во в заказе
                      val quantityCapitalized: String, //Кол-во, которое было оприходовано по этому заказу и этому товару
                      val purchaseOrderUnits: Uom, //ЕИ заказа на поставку
                      val overdToleranceLimit: String, //Граница допуска для сверхпоставки
                      val underdToleranceLimit: String, //Граница допуска при недопоставке
                      val upLimitCondAmount: String, //Верхняя граница суммы условия (МРЦ)
                      val quantityInvest: String, //Числитель для пересчета ЕИ заказа в базовую ЕИ (кол-во вложения)
                      val roundingSurplus: String, //Округления излишки
                      val roundingShortages: String, //Округления недостачи
                      val isNoEAN: Boolean, //Без ШК
                      val isWithoutRecount: Boolean, //Без пересчета
                      val isUFF: Boolean, //Индикатор: UFF (фрукты, овощи)
                      val isNotEdit: Boolean,
                      val generalShelfLife: String, //общий срок годности
                      val remainingShelfLife: String, //остаточный срок годности
                      val isRus: Boolean,
                      val isBoxFl: Boolean,
                      val isMarkFl: Boolean,
                      val isVet: Boolean,
                      val numberBoxesControl: String, //кол-во коробок для контроля
                      val numberStampsControl: String, //кол-во марок для контроля
                      val processingUnit: String,
                      val isGoodsAddedAsSurplus: Boolean, //товар был добавлен в задание ПГЕ как излишек, трелло https://trello.com/c/im9rJqrU
                      val mhdhbDays: Int, //значение из 48 справочника для определения срока годности
                      val mhdrzDays: Int, //значение из 48 справочника для определения срока годности
                      val markType: MarkType, //для маркированного товара
                      val isCountingBoxes: Boolean, //маркированный товар, пусто - нет возможности пересчета в коробах
                      val nestingInOneBlock: String, //маркированный товар, Вложенность в один блок
                      val isControlGTIN: Boolean, //маркированный товар, Контроль GTIN
                      val isGrayZone: Boolean, //маркированный товар
                      val countPiecesBox: String, //маркированный товар, сколько пачек (штук) в одной коробке
                      val numeratorConvertBaseUnitMeasure: Double, //числитель для преобразования в базовую единицу измерения
                      val denominatorConvertBaseUnitMeasure: Double,  //знаменатель для преобразования в базовую единицу измерения
                      val isZBatches: Boolean, //Z-партии
                      val isNeedPrint: Boolean,
                      val alternativeUnitMeasure: String, // альтернативная единица измерения
                      val quantityAlternativeUnitMeasure: Double // количество в альтернативной единице измерения
                        ) : ProductInfo(materialNumber, description, uom, type, isSet, sectionId, matrixType, materialType) {

    fun copy(materialNumber: String = this.materialNumber,
             description: String = this.description,
             uom: Uom = this.uom,
             type:ProductType = this.type,
             isSet: Boolean = this.isSet,
             sectionId: String = this.sectionId,
             matrixType: MatrixType = this.matrixType,
             materialType: String = this.materialType,
             origQuantity: String = this.origQuantity,
             orderQuantity: String = this.orderQuantity,
             quantityCapitalized: String = this.quantityCapitalized,
             purchaseOrderUnits: Uom = this.purchaseOrderUnits,
             overdToleranceLimit: String = this.overdToleranceLimit,
             underdToleranceLimit: String = this.underdToleranceLimit,
             upLimitCondAmount: String = this.upLimitCondAmount,
             quantityInvest: String = this.quantityInvest,
             roundingSurplus: String = this.roundingSurplus,
             roundingShortages: String = this.roundingShortages,
             isNoEAN: Boolean = this.isNoEAN,
             isWithoutRecount: Boolean = this.isWithoutRecount,
             isUFF: Boolean = this.isUFF,
             isNotEdit: Boolean = this.isNotEdit,
             generalShelfLife: String = this.generalShelfLife,
             remainingShelfLife: String = this.remainingShelfLife,
             isRus: Boolean = this.isRus,
             isBoxFl: Boolean = this.isBoxFl,
             isMarkFl: Boolean = this.isMarkFl,
             isVet: Boolean = this.isVet,
             numberBoxesControl: String = this.numberBoxesControl,
             numberStampsControl: String = this.numberStampsControl,
             processingUnit: String = this.processingUnit,
             isGoodsAddedAsSurplus: Boolean = this.isGoodsAddedAsSurplus,
             mhdhbDays: Int = this.mhdhbDays,
             mhdrzDays: Int = this.mhdrzDays,
             markType: MarkType = this.markType,
             isCountingBoxes: Boolean = this.isCountingBoxes,
             nestingInOneBlock: String = this.nestingInOneBlock,
             isControlGTIN: Boolean = this.isControlGTIN,
             isGrayZone: Boolean = this.isGrayZone,
             countPiecesBox: String= this.countPiecesBox,
             numeratorConvertBaseUnitMeasure: Double = this.numeratorConvertBaseUnitMeasure,
             denominatorConvertBaseUnitMeasure: Double = this.denominatorConvertBaseUnitMeasure,
             isZBatches: Boolean = this.isZBatches,
             isNeedPrint: Boolean = this.isNeedPrint,
             alternativeUnitMeasure: String = this.alternativeUnitMeasure,
             quantityAlternativeUnitMeasure: Double = this.quantityAlternativeUnitMeasure) : TaskProductInfo {
        return TaskProductInfo(
                materialNumber = materialNumber,
                description = description,
                uom = uom,
                type = type,
                isSet = isSet,
                sectionId = sectionId,
                matrixType = matrixType,
                materialType = materialType,
                origQuantity = origQuantity,
                orderQuantity = orderQuantity,
                quantityCapitalized = quantityCapitalized,
                purchaseOrderUnits = purchaseOrderUnits,
                overdToleranceLimit = overdToleranceLimit,
                underdToleranceLimit = underdToleranceLimit,
                upLimitCondAmount = upLimitCondAmount,
                quantityInvest = quantityInvest,
                roundingSurplus = roundingSurplus,
                roundingShortages = roundingShortages,
                isNoEAN = isNoEAN,
                isWithoutRecount = isWithoutRecount,
                isUFF = isUFF,
                isNotEdit = isNotEdit,
                generalShelfLife = generalShelfLife,
                remainingShelfLife = remainingShelfLife,
                isRus = isRus,
                isBoxFl = isBoxFl,
                isMarkFl = isMarkFl,
                isVet = isVet,
                numberBoxesControl = numberBoxesControl,
                numberStampsControl = numberStampsControl,
                processingUnit = processingUnit,
                isGoodsAddedAsSurplus = isGoodsAddedAsSurplus,
                mhdhbDays = mhdhbDays,
                mhdrzDays = mhdrzDays,
                markType = markType,
                isCountingBoxes = isCountingBoxes,
                nestingInOneBlock = nestingInOneBlock,
                isControlGTIN = isControlGTIN,
                isGrayZone = isGrayZone,
                countPiecesBox = countPiecesBox,
                numeratorConvertBaseUnitMeasure = numeratorConvertBaseUnitMeasure,
                denominatorConvertBaseUnitMeasure = denominatorConvertBaseUnitMeasure,
                isZBatches = isZBatches,
                isNeedPrint = isNeedPrint,
                alternativeUnitMeasure = alternativeUnitMeasure,
                quantityAlternativeUnitMeasure = quantityAlternativeUnitMeasure
        )
    }
}