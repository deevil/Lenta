package com.lenta.bp12.features.open_task.task_search

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.pojo.TaskSearchParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class TaskSearchViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: IOpenTaskManager

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
    }

    val provider = MutableLiveData("")

    val material = MutableLiveData("")

    val mark = MutableLiveData("")

    val section = MutableLiveData("")

    val searchEnabled = provider.combineLatest(material).combineLatest(mark).combineLatest(section).map {
        it?.let {
            val provider = it.first.first.first
            val good = it.first.first.second
            val mark = it.first.second
            val section = it.second

            provider.isNotEmpty() || good.isNotEmpty() || mark.isNotEmpty() || section.isNotEmpty()
        }
    }

    // -----------------------------

    init {
        launchUITryCatch {
            initSearchFields()
        }
    }

    private fun initSearchFields() {
        manager.searchParams?.let { searchParams ->
            provider.value = searchParams.providerCode
            material.value = searchParams.material
            mark.value = searchParams.exciseMark
            section.value = searchParams.section
        }
    }

    fun onClickSearch() {
        manager.searchParams = TaskSearchParams(
                providerCode = provider.value.orEmpty(),
                material = material.value.orEmpty(),
                section = section.value.orEmpty(),
                exciseMark = mark.value.orEmpty()
        )

        manager.isNeedUpdateTaskList = true
        navigator.goBack()
    }

}
