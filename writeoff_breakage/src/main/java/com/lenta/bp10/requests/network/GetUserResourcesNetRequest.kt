package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class GetUserResourcesNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<UserResourcesResult, UserResourceInfoParams> {

    override suspend fun run(params: UserResourceInfoParams): Either<Failure, UserResourcesResult> {
        return fmpRequestsHelper.restRequest("ZFMP_UTZ_54_V001", params, UserResourceInfoStatus::class.java)
    }
}

class UserResourceInfoStatus : ObjectRawStatus<UserResourcesResult>()

data class UserResourceInfoParams(
        @SerializedName("IV_USER")
        val user: String?
)

class ItemLocal_ET_TASK_TPS {
    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("TASK_TYPE")
    var taskType: String? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("BWART")
    var bwart: String? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("KOSTL")
    var kostl: String? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("LGORTTO")
    var lgortto: String? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("SEND_GIS")
    var sendGis: String? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("NO_GRUND")
    var noGrund: String? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("LONG_NAME")
    var longName: String? = null

    //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
    @SerializedName("LIMIT")
    var limit: Double? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("CHK_OWNPR")
    var chkOwnpr: String? = null
}

class ItemLocal_ET_CNTRL {
    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("TASK_TYPE")
    var taskType: String? = null

    //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
    @SerializedName("TASK_CNTRL")
    var taskCntrl: String? = null
}

data class UserResourcesResult(
        @SerializedName("ET_CNTRL")
        val gisControlls: List<ItemLocal_ET_CNTRL>?,
        @SerializedName("ET_TASK_TPS")
        val taskSettings: List<ItemLocal_ET_TASK_TPS>?,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse