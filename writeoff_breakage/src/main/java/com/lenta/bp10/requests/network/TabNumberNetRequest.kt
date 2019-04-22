package com.lenta.bp10.requests.network

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import com.mobrun.plugin.models.StatusRawDataString
import javax.inject.Inject

class TabNumberNetRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, TabNumberParams>() {
    override suspend fun run(params: TabNumberParams): Either<Failure, Boolean> {
        val webCallParams = WebCallParams()
        webCallParams.data = "IV_PERNR = ${params.tabNumber}"
        val status = hyperHive.requestAPI.web("ZMP_UTZ_98_V001", webCallParams, StatusRawDataString::class.java).execute()
        Logg.d { "status: ${status.result.raw}" }
        return status.toEither()
    }


}

data class TabNumberParams(val tabNumber: String)

data class TabNumberResult(
        val errorText: String? = null,
        val jobName: String? = null,
        val name: String? = null,
        val retCode: Int

)