package com.lenta.bp14.models.work_list.repo

import com.lenta.bp14.models.work_list.CommonGoodInfo
import com.lenta.shared.models.core.Uom

class WorkListRepo {

    fun getCommonGoodInfoByEan(ean: String): CommonGoodInfo? {
        return CommonGoodInfo(
                ean = "11111111",
                material = "000000000000222222",
                matcode = "333333333333",
                name = "Товар",
                unit = Uom.ST,
                goodGroup = "123456",
                purchaseGroup = "1111"
        )
    }

}