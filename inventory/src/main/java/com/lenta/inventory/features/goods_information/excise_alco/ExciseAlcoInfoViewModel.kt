package com.lenta.inventory.features.goods_information.excise_alco

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExciseAlcoInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val editTextFocus: MutableLiveData<Boolean> = MutableLiveData(false)

    //val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData(TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
            false, "1", MatrixType.Active, "materialType","3", null, false))

    val storePlaceNumber: MutableLiveData<String> = MutableLiveData("123456789")
    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { !it.isNullOrEmpty() }

    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData("")

    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull()?: 0.0 }

    val suffix: MutableLiveData<String> = MutableLiveData()

    val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0)
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    fun setProductInfo(productInfo: TaskProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
        }
    }

    fun onClickRollback() {
        //todo
    }

    fun onClickDetails() {
        //todo
    }

    fun onClickMissing() {
        //todo
        //screenNavigator.openGoodsDetailsStorageScreen()
    }

    fun onClickApply() {
        //todo
    }

    fun onScanResult(data: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClickPosition(position: Int) {
        return
    }
}
