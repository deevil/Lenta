package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_EXIDV_TOP Таблица ГЕ (грузовая единица)
data class TaskCargoUnitInfo(
        val cargoUnitNumber: String,
        val cargoUnitStatus: String,
        val palletType: String,
        val isCount: Boolean,
        val isADV: Boolean,
        val isAlco: Boolean,
        val isExc: Boolean,
        val isUFF: Boolean,
        val isRawMaterials: Boolean,
        val isSpecialControlGoods: Boolean,
        val isVet: Boolean,
        val isNoGisControl: Boolean,
        val isStamp: Boolean,
        val quantityPositions: Int
) {
    companion object {
        fun from(restData: TaskCargoUnitInfoRestData): TaskCargoUnitInfo {
            return TaskCargoUnitInfo(
                    cargoUnitNumber = restData.cargoUnitNumber,
                    cargoUnitStatus = restData.cargoUnitStatus,
                    palletType = restData.palletType,
                    isCount = restData.isCount.isNotEmpty(),
                    isADV = restData.isADV.isNotEmpty(),
                    isAlco = restData.isAlco.isNotEmpty(),
                    isExc = restData.isExc.isNotEmpty(),
                    isUFF = restData.isUFF.isNotEmpty(),
                    isRawMaterials = restData.isRawMaterials.isNotEmpty(),
                    isSpecialControlGoods = restData.isSpecialControlGoods.isNotEmpty(),
                    isVet = restData.isVet.isNotEmpty(),
                    isNoGisControl = restData.isNoGisControl.isNotEmpty(),
                    isStamp = restData.isStamp.isNotEmpty(),
                    quantityPositions = restData.quantityPositions.toInt()
            )
        }
    }
}

data class TaskCargoUnitInfoRestData(
        @SerializedName("EXIDV_TOP") //Номер ГЕ
        val cargoUnitNumber: String,
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
        val quantityPositions: String
) {

    companion object {
        fun from(data: TaskCargoUnitInfo): TaskCargoUnitInfoRestData {
            return TaskCargoUnitInfoRestData(
                    cargoUnitNumber = data.cargoUnitNumber,
                    cargoUnitStatus = data.cargoUnitStatus,
                    palletType = data.palletType,
                    isCount = if (data.isCount) "X" else "",
                    isADV = if (data.isADV) "X" else "",
                    isAlco = if (data.isAlco) "X" else "",
                    isExc = if (data.isExc) "X" else "",
                    isUFF = if (data.isUFF) "X" else "",
                    isRawMaterials = if (data.isRawMaterials) "X" else "",
                    isSpecialControlGoods = if (data.isSpecialControlGoods) "X" else "",
                    isVet = if (data.isVet) "X" else "",
                    isNoGisControl = if (data.isNoGisControl) "X" else "",
                    isStamp = if (data.isStamp) "X" else "",
                    quantityPositions = data.quantityPositions.toString()
            )
        }
    }
}