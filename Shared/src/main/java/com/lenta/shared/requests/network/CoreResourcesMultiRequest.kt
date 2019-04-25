package com.lenta.shared.requests.network

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.toEitherBoolean
import com.lenta.shared.utilities.extentions.implementationOf
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import com.mobrun.plugin.models.BaseStatus

abstract class CoreResourcesMultiRequest : UseCase<Boolean, MutableLiveData<LoadStatus>?>() {

    abstract val isDeltaRequest: Boolean


    override suspend fun run(params: MutableLiveData<LoadStatus>?): Either<Failure, Boolean> {

        val requests = getListOfRequests()

        lateinit var either: Either<Failure, Boolean>

        val startTime = System.currentTimeMillis()

        params?.postValue(Loading(startTime = startTime, loadingDataSize = 0L))

        for (request in requests) {
            either = (if (isDeltaRequest) {
                request.streamCallDelta()
            } else {
                request.streamCallTable()
            }).execute().handleResult(status = params).toEitherBoolean()
            if (either.isLeft) {
                params?.postValue(NotInit)
                return either
            }
        }

        params?.postValue(params.value)

        return either

    }

    abstract fun getListOfRequests(): List<RequestBuilder<out CustomParameter, out ScalarParameter<Any>>>

    private fun BaseStatus.handleResult(status: MutableLiveData<LoadStatus>?): BaseStatus {
        Logg.d { "BaseStatus: $this" }
        status?.value.implementationOf(Loading::class.java)?.let {
            val res = it.copy(loadingDataSize = it.loadingDataSize + (this.httpStatus?.bytesDownloaded
                    ?: 0))
            status?.postValue(res)
        }
        return this
    }


}


