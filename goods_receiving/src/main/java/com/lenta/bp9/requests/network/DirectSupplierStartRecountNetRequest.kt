package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class DirectSupplierStartRecountNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<DirectSupplierStartRecountRestInfo, DirectSupplierStartRecountParams> {
    override suspend fun run(params: DirectSupplierStartRecountParams): Either<Failure, DirectSupplierStartRecountRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_11_V001", params, DirectSupplierStartRecountStatus::class.java)
    }
}

data class DirectSupplierStartRecountParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String, //Номер задания
        @SerializedName("IV_IP")
        val deviceIP: String, //Ip адрес ТСД
        @SerializedName("IV_PERNR")
        val personnelNumber: String, //Табельный номер
        @SerializedName("IV_DATE_COUNT")
        val dateRecount: String, //Дата начала пересчета
        @SerializedName("IV_TIME_COUNT")
        val timeRecount: String, //Время начала пересчета
        @SerializedName("IV_UNBIND_VSD")
        val unbindVSD: String, //общий флаг
        @SerializedName("IV_OS")
        val operatingSystem: String //операционная система, Win - 1, Андроид - 2
)

class DirectSupplierStartRecountStatus : ObjectRawStatus<DirectSupplierStartRecountRestInfo>()

data class DirectSupplierStartRecountRestInfo(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_TASK_POS") //Таблица состава задания ППП	ZTT_GRZ_TASK_DS_POS_EXCH
        val taskComposition: List<TaskComposition>,
        @SerializedName("ET_TASK_DIFF") //Таблица расхождений по товару	ZTT_GRZ_TASK_DIF_EXCH
        val taskProductDiscrepancies: List<TaskProductDiscrepanciesRestData>,
        @SerializedName("ET_TASK_PARTS") //Таблица партий задания
        val taskBatches: List<TaskBatchInfoRestData>,
        @SerializedName("ET_PARTS_DIFF") //Таблица расхождений по партиям
        val taskBatchesDiscrepancies: List<TaskBatchesDiscrepanciesRestData>,
        @SerializedName("ET_TASK_BOX") //Список коробок задания для передачи в МП
        val taskBoxes: List<TaskBoxInfoRestData>,
        @SerializedName("ET_BOX_DIFF") //Таблица обработанных коробов
        val taskBoxesDiscrepancies: List<TaskBoxDiscrepanciesRestData>,
        @SerializedName("ET_TASK_MARK") //Список марок задания для передачи в МП
        val taskExciseStamps: List<TaskExciseStampInfoRestData>,
        @SerializedName("ET_MARK_DIFF") //Таблица обработанных марок задания
        val taskExciseStampsDiscrepancies: List<TaskExciseStampDiscrepanciesRestData>,
        @SerializedName("ET_MARK_BAD") //Таблица плохих марок задания
        val taskExciseStampBad: List<TaskExciseStampBadRestData>,
        @SerializedName("ET_VET_DIFF") //Таблица расхождений по вет.товарам
        val taskMercuryDiscrepancies: List<TaskMercuryDiscrepanciesRestData>?,
        @SerializedName("ET_VET_NOT_ACTUAL") //Список не актуальных ВСД
        val taskMercuryNotActualRestData: List<TaskMercuryNotActualRestData>,
        @SerializedName("ET_PROD_TEXT")//Таблица ЕГАИС производителей
        val manufacturers: List<Manufacturer>,
        @SerializedName("ET_AUFNR_LIST")//Таблица данных технологического заказа
        val processOrderData: List<TaskProcessOrderDataRestData>,
        @SerializedName("ET_TASK_SETS")//Список наборов
        val setsInfo: List<TaskSetsRestData>,
        @SerializedName("ET_TASK_PACK")//Список блоков для маркированного товара
        val taskBlocks: List<TaskBlockInfoRestData>,
        @SerializedName("ET_PACK_DIFF")//Список обработанных блоков для маркированного товара
        val taskBlocksDiscrepancies: List<TaskBlockDiscrepanciesRestData>,
        @SerializedName("ET_PROPERTIES")//таблица свойств для маркированного товара
        val markingGoodsProperties: List<TaskMarkingGoodsPropertiesRestData>?,
        @SerializedName("ET_ZPRODUCERS")//Таблица данных производителей для Z-партий
        val manufacturersForZBatches: List<TaskManufacturersForZBatchesRestData>?,
        @SerializedName("ET_TASK_ZPARTS")//Таблица Z-партий задания
        val taskZBatchInfo: List<TaskZBatchInfoRestData>?,
        @SerializedName("ET_ZPARTS_DIFF")//Таблица расхождений по Z-партиям
        val taskZBatchesDiscrepancies: List<TaskZBatchesDiscrepanciesRestData>?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class TaskComposition(
        @SerializedName("MATNR")
        val materialNumber: String, //Сап код товара
        @SerializedName("ORMNG")
        val origDeliveryQuantity: String?, //Исходное количество позиции поставки
        @SerializedName("MEINS")
        val uom: String, //базисная единица измерения
        @SerializedName("MENGE")
        val menge: String, //Кол-во в заказе
        @SerializedName("WEMNG")
        val volumeGoodsReceived: String?, //объем поступившего товара
        @SerializedName("BSTME")
        val purchaseOrderUnits: String, //ЕИ заказа на поставку
        @SerializedName("UEBTO")
        val overDeliveryToleranceLimit: String?, //Граница допуска для сверхпоставки
        @SerializedName("UNTTO")
        val shortDeliveryToleranceLimit: String?, //Граница допуска при недопоставке
        @SerializedName("GKWRT")
        val upperLimitConditionAmount: String, //Верхняя граница суммы условия (МРЦ)
        @SerializedName("QNTINCL")
        val quantityInvestments: String, //кол-во вложения
        @SerializedName("ROUND_LACK")
        val roundingSurplus: String, //Округления излишки
        @SerializedName("ROUND_HEAP")
        val roundingShortages: String, //Округления недостачи
        @SerializedName("NO_EAN")
        val noEAN: String, //Без ШК
        @SerializedName("NO_COUNT")
        val noRecount: String, //Без пересчета
        @SerializedName("IS_UFF")
        val isUFF: String, //Индикатор: UFF (фрукты, овощи)
        @SerializedName("IS_ALCO")
        val isAlco: String, //Признак алкогольного товара
        @SerializedName("IS_EXC")
        val isExc: String, //Признак акцизного алкоголя
        @SerializedName("NOT_EDIT")
        val notEdit: String, //Запрет редактирования
        @SerializedName("MHDHB_NEW")
        val generalShelfLife: String, //общий срок годности
        @SerializedName("MHDRZ_NEW")
        val remainingShelfLife: String, //оставшийся срок годности
        @SerializedName("IS_SET")
        val isSet: String, //признак набора
        @SerializedName("IS_RUS")
        val isRus:String,
        @SerializedName("IS_BOX_FL")
        val BoxFl: String,
        @SerializedName("IS_MARK_FL")
        val isStampFl: String,
        @SerializedName("NUM_BOX_CHK")
        val quantityBoxesControl: String, //количество коробок для контроля
        @SerializedName("NUM_MARK_CHK")
        val quantityStampsControl: String, //количество марок для контроля
        @SerializedName("IS_VET")
        val isVet: String,
        @SerializedName("ABTNR")
        val departmentNumber: String, //номер отдела (abtnr)
        @SerializedName("ZMARKTYPE")
        val markType: String?,
        @SerializedName("EXIDV")
        val processingUnit: String?, //Номер ЕО (Единица обработки) (для 28 реста, в 11 и 15 рестах данного поля нет)
        @SerializedName("IS_USE_ALTERN_MEINS")
        val isCountingBoxes: String, //маркированный товар, пусто - нет возможности пересчета в коробах
        @SerializedName("QNTINCL_PACK")
        val nestingInOneBlock: String?, //маркированный товар, Вложенность в один блок
        @SerializedName("IS_CHECK_GTIN")
        val isControlGTIN: String, //маркированный товар, Контроль GTIN
        @SerializedName("IS_GRAYZONE")
        val isGrayZone: String, //маркированный товар
        @SerializedName("QNTINCL_ALT_BSTME")
        val countPiecesBox: String, //маркированный товар, сколько пачек (штук) в одной коробке
        @SerializedName("IS_ZPARTS")
        val isZBatches: String?, // Z-партии
        @SerializedName("IS_NEEDPRINT")
        val isNeedPrint: String?, // Z-партии, печать этикеток
        @SerializedName("ALTME")
        val alternativeUnitMeasure: String?, // альтернативная единица измерения
        @SerializedName("ALTVOLUME")
        val quantityAlternativeUnitMeasure: String? // количество в альтернативной единице измерения

)
