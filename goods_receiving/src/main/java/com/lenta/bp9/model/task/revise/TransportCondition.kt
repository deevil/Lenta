package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//Таблица контроля условий перевозки
data class TransportCondition(
        val conditionID: String, //ID Условия
        val conditionName: String, //Название условия
        val isObligatory: Boolean,  // Обязательность
        var isCheck: Boolean,   // Отмечено или нет
        var value: String, // Введенное значение, если условие подразумевает ввод
        val conditionType: ConditionType, // Тип условия
        val conditionViewType: ConditionViewType // Тип отображения условия
) {

    companion object {
        fun from(restData: TransportConditionRestData): TransportCondition {
            val conditionType = ConditionType.from(restData.conditionType)
            val isCheck = (conditionType == ConditionType.Checkbox && restData.conditionValue == "X") ||
                    (conditionType == ConditionType.Input && restData.conditionValue.isNotEmpty())
            return TransportCondition(
                    conditionID = restData.conditionID,
                    conditionName = restData.conditionName,
                    isObligatory = restData.isObligatory.isNotEmpty(),
                    isCheck = isCheck,
                    value = restData.conditionValue,
                    conditionType = conditionType,
                    conditionViewType = ConditionViewType.from(restData.conditionViewType)
                    )
        }
    }
}

data class TransportConditionRestData(
        @SerializedName("COND_ID")
        val conditionID: String,
        @SerializedName("COND_NAME")
        val conditionName: String,
        @SerializedName("COND_TYPE")
        val conditionType: String,
        @SerializedName("OBLIGATORY")
        val isObligatory: String,
        @SerializedName("COND_VALUE")
        val conditionValue: String,
        @SerializedName("COND_VIEW")
        val conditionViewType: String) {

    companion object {
        fun from(data: TransportCondition): TransportConditionRestData {
            val value = when (data.conditionType) {
                ConditionType.Checkbox -> if (data.isCheck) "X" else ""
                ConditionType.Input -> data.value
                else -> ""
            }
            return TransportConditionRestData(
                    conditionID = data.conditionID,
                    conditionName = data.conditionName,
                    isObligatory = if (data.isObligatory) "X" else "",
                    conditionType = data.conditionType.conditionTypeString,
                    conditionViewType = data.conditionViewType.conditionViewTypeString,
                    conditionValue = value
            )
        }
    }
}

enum class ConditionType(val conditionTypeString: String) {
    None(""),
    Checkbox("1"),
    Input("2");

    companion object {
        fun from(conditionTypeString: String): ConditionType {
            return when (conditionTypeString) {
                "1" -> Checkbox
                "2" -> Input
                else -> None
            }
        }
    }
}

enum class ConditionViewType(val conditionViewTypeString: String) {
    None(""),
    Seal("0"),
    Simple("1"),
    Temperature("2");

    companion object {
        fun from(conditionViewTypeString: String): ConditionViewType {
            return when (conditionViewTypeString) {
                "0" -> Seal
                "1" -> Simple
                "2" -> Temperature
                else -> None
            }
        }
    }
}