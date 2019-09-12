package com.lenta.bp14.models.check_list

import com.lenta.bp14.models.data.pojo.Good


interface ICheckListRepo {

    fun getGoodByEan(ean: String? = null): Good?

}