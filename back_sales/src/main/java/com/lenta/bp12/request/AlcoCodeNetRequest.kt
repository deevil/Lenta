package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class AlcoCodeNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<List<AlcoCodeRestInfo>, Nothing?> {
    override suspend fun run(params: Nothing?): Either<Failure, List<AlcoCodeRestInfo>> {
        return fmpRequestsHelper.restRequest("ZFMP_UTZ_47_V001", null, AlcoCodeStatus::class.java)
    }
}

class AlcoCodeStatus : ObjectRawStatus<List<AlcoCodeRestInfo>>()

data class AlcoCodeRestInfo(
        @SerializedName("name")
        val name: String,
        @SerializedName("data")
        val data: List<List<String>>
)
