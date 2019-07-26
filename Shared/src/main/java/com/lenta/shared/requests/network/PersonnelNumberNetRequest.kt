package com.lenta.shared.requests.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

class PersonnelNumberNetRequest
@Inject constructor(val fmpRequestsHelper: FmpRequestsHelper) : UseCase<TabNumberInfo, TabNumberParams>() {
    override suspend fun run(params: TabNumberParams): Either<Failure, TabNumberInfo> {

        Logg.d { "searchPersonnelNumber TabNumberParams: [${params.tabNumber}]" }

        val personnelNumber = params.tabNumber.filter { it.isDigit() }

        if (personnelNumber.isEmpty()) {
            return Either.Right(TabNumberInfo(retCode = 0))
        }
        return fmpRequestsHelper.restRequest(
                resourceName = "ZMP_UTZ_98_V001",
                data = "{\"IV_PERNR\":\"$personnelNumber\"}",
                clazz = TabNumberStatus::class.java
        )
    }
}


data class TabNumberParams(val tabNumber: String)

class TabNumberStatus : ObjectRawStatus<TabNumberInfo>()

data class TabNumberInfo(
        @Expose
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String? = null,
        @Expose
        @SerializedName("EV_JOBNAME")
        val jobName: String? = null,
        @Expose
        @SerializedName("EV_NAME")
        val name: String? = null,
        @Expose
        @SerializedName("EV_RETCODE")
        override val retCode: Int

) : SapResponse