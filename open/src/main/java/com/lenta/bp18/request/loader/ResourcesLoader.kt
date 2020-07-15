package com.lenta.bp18.request.loader

import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.request.SlowResourcesMultiRequest
import com.lenta.shared.requests.network.LoadStatus
import com.lenta.shared.requests.network.Loaded
import com.lenta.shared.requests.network.NotInit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ResourcesLoader(val slowResourcesNetRequest: SlowResourcesMultiRequest) {

    val slowResourcesLoadingStatus = MutableLiveData<LoadStatus>(NotInit)

    fun startLoadSlowResources() {
        val loadingStatusValue = slowResourcesLoadingStatus.value.let {
            if (it is NotInit || it is Loaded) {
                //TODO Избавиться от GlobalScope
                GlobalScope.launch {
                    slowResourcesNetRequest(slowResourcesLoadingStatus)
                }
            }
        }
    }
}

