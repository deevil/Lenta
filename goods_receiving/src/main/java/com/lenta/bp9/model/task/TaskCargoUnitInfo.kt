package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_EXIDV_TOP Таблица ГЕ (грузовая единица)
data class TaskCargoUnitInfo(
        val cargoUnitNumber: String,
        val cargoUnitCard: String,
        val cargoUnitStatus: String,
        val palletType: String,
        val isCount: Boolean,
        //val ProductSettlementIndicators: String,
        val isADV: Boolean,
        val isAlco: Boolean,
        val isExc: Boolean,
        val isUFF: Boolean,
        val isRawMaterials: Boolean,
        val isSpecialControlGoods: Boolean,
        val isVet: Boolean,
        val isNoGisControl: Boolean,
        val quantityPositions: Int
) {
    companion object {
        fun from(restData: TaskCargoUnitInfoRestData): TaskCargoUnitInfo {
            return TaskCargoUnitInfo(
                    cargoUnitNumber = restData.cargoUnitNumber,
                    cargoUnitCard = restData.cargoUnitCard,
                    cargoUnitStatus = restData.cargoUnitStatus,
                    palletType = restData.palletType,
                    isCount = restData.isCount.isNotEmpty(),
                    //ProductSettlementIndicators = restData.ProductSettlementIndicators,
                    isADV = restData.isADV.isNotEmpty(),
                    isAlco = restData.isAlco.isNotEmpty(),
                    isExc = restData.isExc.isNotEmpty(),
                    isUFF = restData.isUFF.isNotEmpty(),
                    isRawMaterials = restData.isRawMaterials.isNotEmpty(),
                    isSpecialControlGoods = restData.isSpecialControlGoods.isNotEmpty(),
                    isVet = restData.isVet.isNotEmpty(),
                    isNoGisControl = restData.isNoGisControl.isNotEmpty(),
                    quantityPositions = restData.quantityPositions.toInt()
            )
        }
    }
}

data class TaskCargoUnitInfoRestData(
        @SerializedName("EXIDV_TOP") //Номер ГЕ
        val cargoUnitNumber: String,
        @SerializedName(".INCLUDE") //Структура карточки ГЕ
        val cargoUnitCard: String,
        @SerializedName("STAT_EXIDV") //Статус ГЕ
        val cargoUnitStatus: String,
        @SerializedName("STAT_EXIDV") //Тип поддона
        val palletType: String,
        @SerializedName("IS_COUNT") //Индикатор: Пересчетная ГЕ
        val isCount: String,
        /**@SerializedName(".INCLUDE") //Рачетные индикаторы для товара
        val ProductSettlementIndicators: String,*/
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
        @SerializedName("QNT_POS") //Количество позиций (QNT_POS)
        val quantityPositions: String
) {

    companion object {
        fun from(data: TaskCargoUnitInfo): TaskCargoUnitInfoRestData {
            return TaskCargoUnitInfoRestData(
                    cargoUnitNumber = data.cargoUnitNumber,
                    cargoUnitCard = data.cargoUnitCard,
                    cargoUnitStatus = data.cargoUnitStatus,
                    palletType = data.palletType,
                    isCount = if (data.isCount) "X" else "",
                    //ProductSettlementIndicators = data.ProductSettlementIndicators,
                    isADV = if (data.isADV) "X" else "",
                    isAlco = if (data.isAlco) "X" else "",
                    isExc = if (data.isExc) "X" else "",
                    isUFF = if (data.isUFF) "X" else "",
                    isRawMaterials = if (data.isRawMaterials) "X" else "",
                    isSpecialControlGoods = if (data.isSpecialControlGoods) "X" else "",
                    isVet = if (data.isVet) "X" else "",
                    isNoGisControl = if (data.isNoGisControl) "X" else "",
                    quantityPositions = data.quantityPositions.toString()
            )
        }
    }
}