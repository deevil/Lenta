package com.lenta.bp10.requests.network.loader

import com.lenta.bp10.fmp.resources.gis_control.ZmpUtz35V001Rfc
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc.LimitedScalarParameter.IV_USER
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.requests.network.CoreResourcesMultiRequest
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import javax.inject.Inject

class UserResourcesMultiRequest @Inject constructor(private val hyperHive: HyperHive, private val sessionInfo: ISessionInfo) : CoreResourcesMultiRequest() {

    override val isDeltaRequest = false

    override fun getListOfRequests(): List<RequestBuilder<out CustomParameter, out ScalarParameter<Any>>> {

        val user = sessionInfo.userName

        return listOf(
                ZmpUtz29V001Rfc(hyperHive).newRequest()
                        .addScalar(IV_USER(user)),
                ZmpUtz35V001Rfc(hyperHive)
                        .newRequest()
                        .addScalar(ZmpUtz35V001Rfc.LimitedScalarParameter.IV_USER(user))
        )

    }


}

