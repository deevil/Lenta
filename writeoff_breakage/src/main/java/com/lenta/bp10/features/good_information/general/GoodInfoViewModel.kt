package com.lenta.bp10.features.good_information.general

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.ProcessGeneralProductService
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    private lateinit var productInfo: ProductInfo

    private val processGeneralProductService: ProcessGeneralProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processGeneralProduct(productInfo)!!
    }

    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData()

    val suffix: MutableLiveData<String> = MutableLiveData()

    val totalCount: MutableLiveData<String> = MutableLiveData()

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo = productInfo
    }

    init {
        viewModelScope.launch {

            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffReasonTitles.value = writeOffTask.taskDescription.moveTypes.map { it.name }
                updateTotalCount()
            }
            suffix.value = productInfo.uom.name

        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.postValue(position)
    }

    fun onClickAdd() {
        addGood()
    }


    fun onClickApply() {
        addGood()
        processGeneralProductService.apply()
        screenNavigator.goBack()
    }

    fun onClickDetails() {

    }

    private fun addGood() {
        getCount().let {
            if (it > 0.0) {
                processGeneralProductService.add(getReason(), it)
                updateTotalCount()
                count.value = ""
            }
        }
    }

    private fun updateTotalCount() {
        totalCount.value = (processGeneralProductService.getTotalCount()).toString()
    }

    private fun getCount(): Double {
        return count.value?.toDoubleOrNull() ?: 0.0
    }

    private fun getReason(): WriteOffReason {
        return processGeneralProductService.taskDescription.moveTypes
                .getOrElse(selectedPosition.value ?: -1) { WriteOffReason.empty }
    }

}
