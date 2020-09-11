package com.lenta.bp16.platform.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ZPartDataInfo

interface IZpartInfo {
    val zPartDataInfo: MutableLiveData<List<ZPartDataInfo>>
}