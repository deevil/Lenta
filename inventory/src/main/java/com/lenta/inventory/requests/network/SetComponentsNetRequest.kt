package com.lenta.inventory.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class SetComponentsNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<List<SetComponentsRestInfo>, Nothing?>(){
    override suspend fun run(params: Nothing?): Either<Failure, List<SetComponentsRestInfo>> {

        /**val webCallParams = WebCallParams().apply {
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }*/

        /**val resString = hyperHive.requestAPI.web("ZMP_UTZ_46_V001", webCallParams). execute()
        Logg.d { "resString: $resString" }*/

        //val res = hyperHive.requestAPI.web("ZMP_UTZ_46_V001", webCallParams).execute().toFmpObjectRawStatusEither(SetComponentsStatus::class.java, gson)

        return fmpRequestsHelper.restRequest(
                resourceName = "ZMP_UTZ_46_V001",
                data = null,
                clazz = SetComponentsStatus::class.java
        )
    }
}

class SetComponentsStatus : ObjectRawStatus<List<SetComponentsRestInfo>>()

data class SetComponentsRestInfo(
        @SerializedName("name")
        val name: String,
        @SerializedName("data")
        val data: List<List<String>>
)