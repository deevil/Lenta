package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class GettingDataNewCargoUnitNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<GettingDataNewCargoUnitResult, GettingDataNewCargoUnitParameters> {
    override suspend fun run(params: GettingDataNewCargoUnitParameters): Either<Failure, GettingDataNewCargoUnitResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_22_V001", params, GettingDataNewCargoUnitStatus::class.java)
    }
}

data class GettingDataNewCargoUnitParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_EXIDV")
        val cargoUnitNumber: String
)

class GettingDataNewCargoUnitStatus : ObjectRawStatus<GettingDataNewCargoUnitResult>()

data class GettingDataNewCargoUnitResult(
        @SerializedName("ES_EXIDV")
        val cargoUnitStructure: TaskNewCargoUnitInfoRestData,
        @SerializedName("EV_EXIDV_TYPE")
        val cargoUnitType: String,
        @SerializedName("EV_WERKS")
        val marketNumber: String,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class TaskNewCargoUnitInfoRestData(
        @SerializedName("STAT_EXIDV") //Статус ГЕ
        val cargoUnitStatus: String,
        @SerializedName("TYPE_PALLET") //Тип поддона
        val palletType: String,
        @SerializedName("IS_COUNT") //Индикатор: Пересчетная ГЕ
        val isCount: String,
        @SerializedName("IS_ADV") //Индикатор: Промо
        val isADV: String,
        @SerializedName("IS_ALCO") //Индикатор: Алкоголь
        val isAlco: String,
        @SerializedName("IS_EXC") //Признак – товар акцизный
        val isExc: String,
        @SerializedName("IS_UFF") //Индикатор: UFF (фрукты, овощи)
        val isUFF: String,
        @SerializedName("IS_RAWMAT") //Индикатор: Сырье
        val isRawMaterials: String,
        @SerializedName("IS_SPECIAL") //Индикатор: Товары особого контроля
        val isSpecialControlGoods: String,
        @SerializedName("IS_VET") //Индикатор: Ветконтроль
        val isVet: String,
        @SerializedName("IS_GIS_NO") //Индикатор: Без ГИС-контроля
        val isNoGisControl: String,
        @SerializedName("IS_MARK") //Индикатор: марки
        val isStamp: String,
        @SerializedName("QNT_POS") //Количество позиций (QNT_POS)
        val quantityPositions: String?,
        @SerializedName("IS_PACK") //товар для упаковки
        val isPack: String,
        @SerializedName("LGORT") //Склад
        val stock: String
) {
        companion object {
                fun inCargoUnitInfo(data: TaskNewCargoUnitInfoRestData, cargoUnitNumber: String): TaskCargoUnitInfo {
                        return TaskCargoUnitInfo(
                                cargoUnitNumber = cargoUnitNumber,
                                cargoUnitStatus = data.cargoUnitStatus,
                                palletType = data.palletType,
                                isCount = data.isCount.isNotEmpty(),
                                isADV = data.isADV.isNotEmpty(),
                                isAlco = data.isAlco.isNotEmpty(),
                                isExc = data.isExc.isNotEmpty(),
                                isUFF = data.isUFF.isNotEmpty(),
                                isRawMaterials = data.isRawMaterials.isNotEmpty(),
                                isSpecialControlGoods = data.isSpecialControlGoods.isNotEmpty(),
                                isVet = data.isVet.isNotEmpty(),
                                isNoGisControl = data.isNoGisControl.isNotEmpty(),
                                isStamp = data.isStamp.isNotEmpty(),
                                quantityPositions = data.quantityPositions ?: "",
                                isPack = data.isPack.isNotEmpty(),
                                stock = data.stock
                        )
                }
        }
}