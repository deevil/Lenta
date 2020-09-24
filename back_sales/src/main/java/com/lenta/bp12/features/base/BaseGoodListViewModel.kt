package com.lenta.bp12.features.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class BaseGoodListViewModel<T : Taskable> : CoreViewModel() {

    val numberField: MutableLiveData<String> = MutableLiveData("")

    abstract val task: MutableLiveData<T>
    abstract var navigator: IScreenNavigator
    abstract var sessionInfo: ISessionInfo
    abstract var database: IDatabaseRepository
    abstract var resource: IResourceManager
    abstract var goodInfoNetRequest: GoodInfoNetRequest
    abstract var markManager: IMarkManager

    abstract fun checkSearchNumber(number: String)
    abstract fun getGoodByEan(ean: String)
    abstract fun setFoundGood(foundGood: Good)

    fun checkThatNoneOfGoodAreMarkType(goodTitle: String) {
        if (task.value?.goods?.none { it.isMarked() } == true) {
            navigator.showForGoodNeedScanFirstMark(goodTitle)
        }
    }

    fun onScanResult(data: String) {
        checkSearchNumber(data)
    }
}