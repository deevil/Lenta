package com.lenta.bp9.features.goods_information.baseGoods

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.extentions.map

interface IBaseDefectable : IBaseVariables{
    val isDefect: MutableLiveData<Boolean>
        get() = spinQualitySelectedPosition.map { it != 0 }
}