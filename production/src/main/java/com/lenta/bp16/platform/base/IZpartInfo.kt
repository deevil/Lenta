package com.lenta.bp16.platform.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI

interface IZpartInfo {
    val zPartDataInfos: MutableLiveData<List<ZPartDataInfoUI>>
}