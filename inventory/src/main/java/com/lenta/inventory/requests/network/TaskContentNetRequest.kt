package com.lenta.inventory.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.inventory.models.StorePlaceStatus
import com.lenta.inventory.models.task.TaskContents
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.models.task.TaskStorePlaceInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.resources.dao_ext.getMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.ExciseStamp
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import org.apache.commons.lang3.ObjectUtils
import javax.inject.Inject

class TaskContentNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<TaskContents, TaskContentParams>(){

    private val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    private val zmpUtz30V001: ZmpUtz30V001 by lazy {
        ZmpUtz30V001(hyperHive)
    }

    override suspend fun run(params: TaskContentParams): Either<Failure, TaskContents> {
        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        val status = hyperHive.requestAPI.web("ZMP_UTZ_96_V001", webCallParams, TaskContentStatus::class.java).execute()
        if (status.isNotBad()) {
            status.result?.raw?.let {
                val products = it.productsList.mapNotNull {
                    val materialInfo = zmpUtz30V001.getMaterial(it.materialNumber)
                    val uomInfo = zmpUtz07V001.getUomInfo(materialInfo?.buom)
                    it.ProductInfo(materialInfo, uomInfo)
                }
                val storePlaces = it.storePlacesList.mapNotNull { it.StorePlaceInfo() }
                val exciseStamps = it.stampsList.mapNotNull { it.ExciseStamp() }
                return Either.Right(TaskContents(products = products, storePlaces = storePlaces, exciseStamps = exciseStamps, deadline = it.timeToProcess))
            }
        }

        return Either.Left(status.getFailure())
    }

    fun TaskProductRestInfo.ProductInfo(materialInfo: ZmpUtz30V001.ItemLocal_ET_MATERIALS?, uomInfo: ZmpUtz07V001.ItemLocal_ET_UOMS?) : TaskProductInfo? {
        if (materialInfo == null || uomInfo == null || materialNumber.isEmpty()) {
            return null
        }
        return  TaskProductInfo(materialNumber = materialInfo.material,
                description = materialInfo.name,
                uom = Uom(code = uomInfo.uom, name = uomInfo.name),
                type = getProductType(isAlco = materialInfo.isAlco.isNotEmpty(), isExcise = materialInfo.isExc.isNotEmpty()),
                isSet = isSet.isNotEmpty(),
                sectionId = materialInfo.abtnr,
                matrixType = getMatrixType(materialInfo.matrType),
                materialType = materialInfo.matype,
                placeCode = storePlaceCode,
                factCount = factQuantity.toDoubleOrNull() ?: 0.0,
                isPositionCalc = positionCounted.isNotEmpty(),
                isExcOld = false //TODO: actual value
        )
    }

    fun TaskStorePlaceRestInfo.StorePlaceInfo() : TaskStorePlaceInfo? {
        if (placeCode.isEmpty()) {
            return null
        }
        return TaskStorePlaceInfo(placeCode = placeCode,
                status = StorePlaceStatus.from(state),
                lockUser = lockUser,
                lockIP = lockIP
        )
    }

    fun TaskExciseStampRestInfo.ExciseStamp() : TaskExciseStamp? {
        return  TaskExciseStamp(materialNumber = productNumber,
                code = markNumber,
                placeCode = storePlaceCode,
                boxNumber = boxNumber,
                setMaterialNumber = productNumberOSN,
                manufacturerCode = organizationCodeEGAIS,
                bottlingDate = dateOfPour,
                isBadStamp = isUnknown.isNotEmpty())
    }
}

data class TaskContentParams(
        @SerializedName("IV_IP")
        val ip: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_MODE")
        val mode: String,
        @SerializedName("IV_PERNR")
        val userNumber: String,
        @SerializedName("IV_PERNR_RELOCK")
        val numberRelock: String,
        @SerializedName("IV_MATNR_DATA_FLG")
        val additionalDataFlag: String,
        @SerializedName("IT_NEW_MATNR_LIST")
        val newProductNumbers: List<String>)

class TaskContentStatus : ObjectRawStatus<TaskContentRestInfo>()

data class TaskContentRestInfo(
        @SerializedName("ET_TASK_MATNR_LIST")
        val productsList: List<TaskProductRestInfo>, //УТЗ ТСД. Инв.: Список товаров для обмена с ТСД
        @SerializedName("ET_PLACES_INFO")
        val storePlacesList: List<TaskStorePlaceRestInfo>, //УТЗ ТСД. Инв.: Таблица информации по МХ задания
        @SerializedName("EV_MIN_UPD_SALES")
        val minUpdSales: String, //Натуральное число ??
        @SerializedName("EV_TIME_OF_PROC")
        val timeToProcess: String, //УТЗ ТСД. Инв.: Время на обработку задания (строка)
        @SerializedName("ET_TASK_MARK_LIST")
        val stampsList: List<TaskExciseStampRestInfo>, //Список марок задания для передачи в МП
        @SerializedName("ET_MATERIALS")
        val fullProductsInfoList: List<TaskFullProductInfo>, //Справочные данные товаров задания
        @SerializedName("ET_SET")
        val setsList: List<TaskSet>, //Список наборов
        @SerializedName("ET_ALCOD_LIST")
        val alcoCodesList: List<TaskAlcoCode>, //Список алкокодов
        @SerializedName("EV_ERROR_TEXT")
        val error: String, //Текст ошибки
        @SerializedName("EV_RETCODE")
        val retcode: String //Код возврата для ABAP-операторов
)

//ET_TASK_MATNR_LIST
data class TaskProductRestInfo(
        @SerializedName("MATNR")
        val materialNumber: String, //Номер товара
        @SerializedName("PLACE_CODE")
        val storePlaceCode: String, //Код места хранения
        @SerializedName("FACT_QNT")
        val factQuantity: String, //УТЗ ТСД: Фактическое количество в БЕИ (первый подсчет)
        @SerializedName("XZAEL")
        val positionCounted: String, //УТЗ ТСД: Инв.: Позиция подсчитана (первый подсчет)
        @SerializedName("IS_DEL")
        val isDel: String, //Общий флаг
        @SerializedName("IS_SET")
        val isSet: String, //Общий флаг
        @SerializedName("IS_EXC_OLD")
        val isExcOld: String //Общий флаг
)

//ET_PLACES_INFO
data class TaskStorePlaceRestInfo(
        @SerializedName("PLACE_CODE")
        val placeCode: String, //Код места хранения
        @SerializedName("NUM_POS")
        val numPos: String, //Натуральное число
        @SerializedName("PLACE_STAT")
        val state: String, //Индикатор из одной позиции
        @SerializedName("LOCK_USER")
        val lockUser: String, //Имя пользователя
        @SerializedName("LOCK_IP")
        val lockIP: String //IP адрес ТСД
)

//ET_TASK_MARK_LIST
data class TaskExciseStampRestInfo(
        @SerializedName("MATNR")
        val productNumber: String, //Номер товара
        @SerializedName("PLACE_CODE")
        val storePlaceCode: String, //Код места хранения
        @SerializedName("MARK_NUM")
        val markNumber: String, //Код акцизной марки
        @SerializedName("BOX_NUM")
        val boxNumber: String, //Номер коробки
        @SerializedName("MATNR_OSN")
        val productNumberOSN: String, //Номер товара ??
        @SerializedName("ZPROD")
        val organizationCodeEGAIS: String, //ЕГАИС Код организации
        @SerializedName("BOTT_MARK")
        val dateOfPour: String, //УТЗ ТСД: Дата розлива
        @SerializedName("IS_UNKNOWN")
        val isUnknown: String //Общий флаг
)

//ET_MATERIALS
data class TaskFullProductInfo(
        @SerializedName("MATERIAL")
        val materialNumber: String, //Номер товара
        @SerializedName("NAME")
        val name: String, //Длинное наименование
        @SerializedName("MATYPE")
        val productType: String, //Вид товара
        @SerializedName("BUOM")
        val units: String, //Базисная единица измерения
        @SerializedName("MATR_TYPE")
        val matrixTypeSKU: String, //Тип матрицы SKU
        @SerializedName("ABTNR")
        val departmentNumber: String, //Номер отдела
        @SerializedName("IS_EXC")
        val isExc: String, //Признак – товар акцизный
        @SerializedName("IS_ALCO")
        val isAlco: String //Общий флаг
)

//ET_SET
data class TaskSet(
        @SerializedName("MATNR_OSN")
        val materialNumber: String, //Номер товара
        @SerializedName("MATNR")
        val specComponent: String, //Компонент спецификации
        @SerializedName("MENGE")
        val nestedQuantity: String, //Количество вложенного
        @SerializedName("MEINS")
        val units: String //Базисная единица измерения
)

//ET_ALCOD_LIST
data class TaskAlcoCode(
        @SerializedName("MATNR")
        val materialNumber: String, //Номер товара
        @SerializedName("ZALCCOD")
        val alcoEGAISCode: String //Номер товара в ЕГАИС (АлкоКод)
)
