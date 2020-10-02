package com.lenta.bp14.features.long_z_part

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.shared.platform.viewmodel.CoreViewModel

class LongZPartInfoViewModel : CoreViewModel() {
    val zPart = MutableLiveData<ZPartUi>()
    fun initZPart(zPart: ZPartUi?) {
        this.zPart.value = zPart
    }
}