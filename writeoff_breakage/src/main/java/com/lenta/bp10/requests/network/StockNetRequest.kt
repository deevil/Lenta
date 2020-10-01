package com.lenta.bp10.requests.network

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class StockNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper, private val sessionInfo: ISessionInfo) : UseCase<StockLockRequestResult, Nothing?> {
    override suspend fun run(params: Nothing?): Either<Failure, StockLockRequestResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = "ZMP_UTZ_02_V001",
                data = mapOf("IV_PLANT" to sessionInfo.market),
                clazz = StockLockRequestStatus::class.java
        )
    }
}


class StockLockRequestStatus : ObjectRawStatus<StockLockRequestResult>()

data class StockLockRequestResult(
        @Nullable
        @SerializedName("ET_STORLOCS")
        val stocksLocks: List<StockLock>?
)

data class StockLock(
        @SerializedName("STORLOC")
        val storloc: String?,

        @SerializedName("LOCKED")
        val locked: String?
)