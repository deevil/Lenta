package com.lenta.bp16.features.add_attribute

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ZPartDataInfo

interface IZpartInfo {
    val zPartDataInfo: MutableLiveData<List<ZPartDataInfo>>
}