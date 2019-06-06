package com.lenta.bp10.features.good_information.general

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.IGoodInformationRepo
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.ProcessGeneralProductService
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.resources.IStringResourceManager
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var resourceManager: IStringResourceManager
    @Inject
    lateinit var goodInformationRepo: IGoodInformationRepo

    private val processGeneralProductService: ProcessGeneralProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processGeneralProduct(productInfo.value!!)!!
    }

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()

    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData("")

    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val suffix: MutableLiveData<String> = MutableLiveData()

    val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0) + processGeneralProductService.getTotalCount()
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(selectedPosition).map {
        val count = it?.first ?: 0.0
        var enabled = false
        productInfo.value?.let { productInfoVal ->
            val reason = getReason()
            enabled =
                    count != 0.0
                            &&
                            reason != WriteOffReason.empty
                            &&
                            processGeneralProductService.taskRepository.getTotalCountForProduct(productInfoVal, getReason()) + (countValue.value
                            ?: 0.0) >= 0.0
        }
        enabled
    }

    val enabledDetailsButton: MutableLiveData<Boolean> = totalCount.map {
        processGeneralProductService.getTotalCount() > 0.0
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {

            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffTask.taskDescription.moveTypes.let { reasons ->
                    if (reasons.isEmpty()) {
                        writeOffReasonTitles.value = listOf(resourceManager.emptyCategory())
                    } else {

                        productInfo.value?.let { it ->
                            val defaultReason = goodInformationRepo.getDefaultReason(
                                    taskType = processServiceManager.getWriteOffTask()!!.taskDescription.taskType.code,
                                    sectionId = it.sectionId,
                                    materialNumber = it.materialNumber
                            )

                            writeOffReasonTitles.value = mutableListOf("").apply {
                                addAll(writeOffTask.taskDescription.moveTypes.map { it.name })
                            }

                            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                                onClickPosition(
                                        writeOffTask
                                                .taskDescription
                                                .moveTypes.indexOfFirst { reason -> reason.code == defaultReason } + 1)
                            }
                        }
                    }
                }
            }

            suffix.value = productInfo.value?.uom?.name

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

        productInfo.value?.let {
            screenNavigator.openGoodsReasonsScreen(productInfo = it)
        }

    }

    private fun addGood() {
        countValue.value?.let {
            processGeneralProductService.add(getReason(), it)
            count.value = ""
        }
    }


    private fun getReason(): WriteOffReason {
        if (processGeneralProductService.taskDescription.moveTypes.isEmpty()) {
            return WriteOffReason(code = "", name = resourceManager.emptyCategory())
        }
        return processGeneralProductService.taskDescription.moveTypes
                .getOrElse((selectedPosition.value ?: 0) - 1) { WriteOffReason.empty }
    }

    fun onBackPressed() {
        processGeneralProductService.discard()
    }

}
