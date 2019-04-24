package com.lenta.bp10.requests.network.loader

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.requests.network.SlowResourcesMultiRequest
import com.lenta.shared.requests.network.LoadStatus
import com.lenta.shared.requests.network.Loaded
import com.lenta.shared.requests.network.NotInit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ResourcesLoader(val slowResourcesNetRequest: SlowResourcesMultiRequest) {

    val slowResourcesLoadingStatus = MutableLiveData<LoadStatus>(NotInit)

    fun startLoadSlowResources() {
        slowResourcesLoadingStatus.value.let {
            if (it is NotInit || it is Loaded) {
                GlobalScope.launch {
                    slowResourcesNetRequest(slowResourcesLoadingStatus)
                }
            }
        }


    }


}

