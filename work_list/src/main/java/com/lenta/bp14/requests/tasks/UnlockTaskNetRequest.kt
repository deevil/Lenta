package com.lenta.bp14.requests.tasks

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.RetCode
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

@AppScope
class UnlockTaskNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : IUnlockTaskNetRequest {

    override suspend fun run(params: UnlockTaskParams): Either<Failure, Boolean> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_10_V001", params, UnlockTaskStatus::class.java).rightToLeft {
            if (it.retCode == 1) {
                return@rightToLeft Failure.SapError(message = it.errorText)
            }
            null
        }.map { true }
    }
}

class UnlockTaskStatus : ObjectRawStatus<RetCode>()

data class UnlockTaskParams(
        @SerializedName("IV_IP")
        val ip: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)

interface IUnlockTaskNetRequest : UseCase<Boolean, UnlockTaskParams>

