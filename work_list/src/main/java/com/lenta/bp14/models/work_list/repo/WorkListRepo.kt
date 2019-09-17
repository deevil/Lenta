package com.lenta.bp14.models.work_list.repo

import com.lenta.bp14.models.check_list.Good


class WorkListRepo : IWorkListRepo {

    override fun getGoodByEan(ean: String): Good? {
        return null
    }

}

interface IWorkListRepo {
    fun getGoodByEan(ean: String): Good?
}