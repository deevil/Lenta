package com.lenta.bp10.requests.network.loader

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.requests.network.FastResourcesMultiRequest
import com.lenta.bp10.requests.network.SlowResourcesMultiRequest
import com.lenta.shared.requests.network.LoadStatus
import com.lenta.shared.requests.network.NotInit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ResourcesLoader(val fastResourcesNetRequest: FastResourcesMultiRequest,
                      val slowResourcesNetRequest: SlowResourcesMultiRequest) {

    val fastResourcesLoadingStatus = MutableLiveData<LoadStatus>(NotInit)

    val slowResourcesLoadingStatus = MutableLiveData<LoadStatus>(NotInit)

    fun startLoadFastResources(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            fastResourcesNetRequest(fastResourcesLoadingStatus)
        }
    }

    fun startLoadSlowResources(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            slowResourcesNetRequest(slowResourcesLoadingStatus)
        }
    }


}

