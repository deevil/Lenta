package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

data class TaskDriverDataInfo (
        val initials: String,
        val passportData: String,
        val carMake: String,
        val carNumber: String,
        val additionalCarNumber: String,
        val transportCompanyCode: String
) {
    companion object {
        fun from(restData: TaskDriverDataInfoRestData): TaskDriverDataInfo {
            return TaskDriverDataInfo(
                    initials = restData.initials,
                    passportData = restData.passportData,
                    carMake = restData.carMake,
                    carNumber = restData.carNumber,
                    additionalCarNumber = restData.additionalCarNumber,
                    transportCompanyCode = restData.transportCompanyCode
            )
        }
    }
}

data class TaskDriverDataInfoRestData(
        @SerializedName("INITIALS") //ФИО водителя
        val initials: String,
        @SerializedName("PASSPORT") //Паспортные данные
        val passportData: String,
        @SerializedName("CAR") //Марка машины
        val carMake: String,
        @SerializedName("CAR_NUM") //Номер машины
        val carNumber: String,
        @SerializedName("CAR_NUM_DOP") //Дополнительный номер машины
        val additionalCarNumber: String,
        @SerializedName("TCOMP_CODE") //Код транспортной компании
        val transportCompanyCode: String
) {

    companion object {
        fun from(data: TaskDriverDataInfo): TaskDriverDataInfoRestData {
            return TaskDriverDataInfoRestData(
                    initials = data.initials,
                    passportData = data.passportData,
                    carMake = data.carMake,
                    carNumber = data.carNumber,
                    additionalCarNumber = data.additionalCarNumber,
                    transportCompanyCode = data.transportCompanyCode
            )
        }
    }
}