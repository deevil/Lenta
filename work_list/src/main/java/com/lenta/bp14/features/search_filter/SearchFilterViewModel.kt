package com.lenta.bp14.features.search_filter

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

class SearchFilterViewModel : CoreViewModel() {

    val taskName = MutableLiveData("Рабочий список от 23.07.19 23:15")

    val section = MutableLiveData("")
    val groupOfGoods = MutableLiveData("")
    val storagePlace = MutableLiveData("")
    val comment = MutableLiveData("")

}
