package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class DirectSupplierStartRecountNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<DirectSupplierStarRecountRestInfo, DirectSupplierStarRecountParams> {
    override suspend fun run(params: DirectSupplierStarRecountParams): Either<Failure, DirectSupplierStarRecountRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_11_V001", params, DirectSupplierStarRecountStatus::class.java)
    }
}

data class DirectSupplierStarRecountParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String, //Номер задания
        @SerializedName("IV_IP")
        val ip: String, //Ip адрес ТСД
        @SerializedName("IV_PERNR")
        val personnelNumber: String, //Табельный номер
        @SerializedName("IV_DATE_COUNT")
        val dateRecount: String, //Дата начала пересчета
        @SerializedName("IV_TIME_COUNT")
        val timeRecount: String, //Время начала пересчета
        @SerializedName("IV_UNBIND_VSD")
        val unbindVSD: String //общий флаг
)

class DirectSupplierStarRecountStatus : ObjectRawStatus<DirectSupplierStarRecountRestInfo>()

data class DirectSupplierStarRecountRestInfo(
        @SerializedName("ET_TASK_POS") //Таблица состава задания ППП	ZTT_GRZ_TASK_DS_POS_EXCH
        val taskComposition: List<TaskComposition>,
        @SerializedName("ET_TASK_DIFF") //Таблица расхождений по товару	ZTT_GRZ_TASK_DIF_EXCH
        val productDiscrepancies: List<ProductDiscrepancies>,
        @SerializedName("ET_TASK_PARTS") //Таблица партий задания	ZTT_GRZ_TASK_PARTS_EXCH
        val taskBatch: List<TaskBatch>,
        @SerializedName("ET_PARTS_DIFF") //Таблица расхождений по партиям	ZTT_GRZ_PARTS_DIF_EXCH
        val batchDiscrepancies: List<BatchDiscrepancies>,
        @SerializedName("ET_PROD_TEXT") //Таблица названий производителей	ZTT_GRZ_PROD_TEXT
        val manufacturers: List<Manufacturer>,
        @SerializedName("ET_TASK_SETS") //список наборов
        val taskSets: String,
        @SerializedName("ET_TASK_BOX") //список коробок задания
        val taskBoxes: String,
        @SerializedName("ET_TASK_MARK") //список марок задания
        val taskStamps: String,
        @SerializedName("ET_MARK_DIFF") //таблица обработанных марок
        val processedStamps: String,
        @SerializedName("ET_MARK_BAD") //таблица плохих марок
        val badStamps: String,
        @SerializedName("ET_BOX_DIFF") //таблица обработанных коробов
        val processedBoxes: String,
        @SerializedName("ET_BOX_DIFF") //структура карточки задания
        val taskStructure: String,
        @SerializedName("ET_VET_NOT_ACTUAL") //таблица неактуальных ВСД
        val vetNotActual: String,
        @SerializedName("ET_VET_DIFF") //таблица расхождений по вет. товарам
        val vetDiscrepancies: String,
        @SerializedName("ET_VBELN_COM") //таблица примечаний к ВП
        val vetNotes: String,
        @SerializedName("EV_RETCODE")
        val retcode: String,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String
)

data class TaskComposition(
        val materialNumber: String, //Сап код товара
        val origDeliveryQuantity: String, //Исходное количество позиции поставки
        val uom: String, //базисная единица измерения
        val menge: String, //Кол-во в заказе
        val volumeGoodsReceived: String, //объем поступившего товара
        val purchaseOrderUnits: String, //ЕИ заказа на поставку
        val overDeliveryToleranceLimit: String, //Граница допуска для сверхпоставки
        val shortDeliveryToleranceLimit: String, //Граница допуска при недопоставке
        val upperLimitConditionAmount: String, //Верхняя граница суммы условия (МРЦ)
        val quantityInvestments: String, //кол-во вложения
        val roundingSurplus: String, //Округления излишки
        val roundingShortages: String, //Округления недостачи
        val noEAN: String, //Без ШК
        val noRecount: String, //Без пересчета
        val isUFF: String, //Индикатор: UFF (фрукты, овощи)
        val isAlco: String, //Признак алкогольного товара
        val isExc: String, //Признак акцизного алкоголя
        val notEdit: String, //Запрет редактирования
        val generalShelfLife: String, //общий срок годности
        val remainingShelfLife: String, //оставшийся срок годности
        val isSet: String, //признак набора
        val isRus:String,
        val BoxFl: String,
        val isStampFl: String,
        val quantityBoxesControl: String, //количество коробок для контроля
        val quantityStampsControl: String, //количество марок для контроля
        val isVet: String,
        val departmentNumber: String //номер отдела (abtnr)
)

data class ProductDiscrepancies(
        val materialNumber: String, //Сап код товара
        val processingUnit: String, //Единица обработки
        val quantityDiscrepancies: String, //кол-во расхождения
        val uom: String, //базисная единица измерения
        val typeDiscrepancy: String, //Тип расхождения
        val notEdit: String //Запрет редактирования
)

data class TaskBatch(
        val materialNumber: String, //Сап код товара
        val batchNumber: String, //номер партии
        val alcoCode: String,
        val manufacturer: String,
        val bottlingDate: String,
        val quantityBatchPlan: String //Кол-во по партии план
)

data class BatchDiscrepancies(
        val materialNumber: String, //Сап код товара
        val batchNumber: String, //номер партии
        val quantityDiscrepancies: String, //кол-во расхождения
        val uom: String, //базисная единица измерения
        val typeDiscrepancy: String, //Тип расхождения
        val notEdit: String, //Запрет редактирования
        val exciseStampCode: String, //Код акцизной марки(73 символа)
        val fullDM: String //DM акцизной марки
)
