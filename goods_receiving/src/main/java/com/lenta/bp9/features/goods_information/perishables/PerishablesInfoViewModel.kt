package com.lenta.bp9.features.goods_information.perishables

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessPerishablesService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PerishablesInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processPerishablesService: ProcessPerishablesService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinShelfLife: MutableLiveData<List<String>> = MutableLiveData()
    val spinShelfLifeSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val shelfLifeDate: MutableLiveData<String> = MutableLiveData("")
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> by lazy  {
        countValue.combineLatest(spinQualitySelectedPosition).map{
            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0) + taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
            } else {
                0.0
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> by lazy  {
        countValue.
                combineLatest(spinReasonRejectionSelectedPosition).
                combineLatest(spinQualitySelectedPosition).
                map{
                    if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
                        (it?.first?.first ?: 0.0) + taskManager.
                                getReceivingTask()!!.
                                taskRepository.
                                getProductsDiscrepancies().
                                getCountRefusalOfProductOfReasonRejection(productInfo.value!!, reasonRejectionInfo.value?.get(it?.first?.second ?: 0) ?.code)
                    } else {
                        0.0
                    }
                }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.
            map {
                it!! != 0.0
            }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            generalShelfLife.value = productInfo.value?.generalShelfLife
            remainingShelfLife.value = productInfo.value?.remainingShelfLife
            qualityInfo.value = dataBase.getQualityInfo()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            spinShelfLife.value = dataBase.getTermControlInfo()
            if (processPerishablesService.newProcessPerishablesService(productInfo.value!!) == null){
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    fun onClickDetails(){
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {
        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
            processPerishablesService.add(acceptTotalCount.value!!.toString(), "1")
        } else {
            processPerishablesService.add(refusalTotalCount.value!!.toString(), reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code)
        }

        count.value = "0"
    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true)
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int){
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    fun onClickPositionSpinShelfLife(position: Int){
        spinShelfLifeSelectedPosition.value = position
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            spinReasonRejectionSelectedPosition.value = 0
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            spinReasonRejection.value = reasonRejectionInfo.value?.map {
                it.name
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun isCorrectDate(checkDate: String?): Boolean {
        return try {
            val formatter = SimpleDateFormat("dd.MM.yyyy")
            val date = formatter.parse(checkDate)
            !(checkDate != formatter.format(date) || date!! > Date())
        } catch (e: Exception) {
            false
        }
    }
}
