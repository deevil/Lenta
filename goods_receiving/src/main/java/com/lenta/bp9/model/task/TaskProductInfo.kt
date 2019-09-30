package com.lenta.bp9.model.task

import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom

//ET_TASK_POS Таблица состава задания ППП (ZSGRZ_TASK_DS_POS_EXCH)
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
                      val totalExpirationDate: String, //общий срок годности
                      val remainingShelfLife: String, //остаточный срок годности
                      val isRus: Boolean,
                      val isBoxFl: Boolean,
                      val isMarkFl: Boolean,
                      val isVet: Boolean,
                      val numberBoxesControl: String, //кол-во коробок для контроля
                      val numberStampsControl: String //кол-во марок для контроля
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
             totalExpirationDate: String = this.totalExpirationDate,
             remainingShelfLife: String = this.remainingShelfLife,
             isRus: Boolean = this.isRus,
             isBoxFl: Boolean = this.isBoxFl,
             isMarkFl: Boolean = this.isMarkFl,
             isVet: Boolean = this.isVet,
             numberBoxesControl: String = this.numberBoxesControl,
             numberStampsControl: String = this.numberStampsControl) : TaskProductInfo {
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
                totalExpirationDate = totalExpirationDate,
                remainingShelfLife = remainingShelfLife,
                isRus = isRus,
                isBoxFl = isBoxFl,
                isMarkFl = isMarkFl,
                isVet = isVet,
                numberBoxesControl = numberBoxesControl,
                numberStampsControl = numberStampsControl
        )
    }
}