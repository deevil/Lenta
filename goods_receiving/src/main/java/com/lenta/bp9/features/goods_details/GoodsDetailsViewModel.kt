package com.lenta.bp9.features.goods_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel

class GoodsDetailsViewModel : CoreViewModel() {

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val goodsDetail: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()

}
