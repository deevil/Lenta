package com.lenta.bp14.models.check_list.repo

import com.lenta.bp14.models.check_list.GoodInfo
import com.lenta.shared.models.core.Uom
import kotlin.random.Random

class CheckListRepo : ICheckListRepo {

    override fun getGoodInfoByMaterial(material: String): GoodInfo? {
        return if (Random.nextBoolean()) {
            GoodInfo(
                    ean = "1234567890",
                    material = "000000000000999921",
                    name = "Товар штучный ",
                    uom = Uom.DEFAULT
            )
        } else {
            GoodInfo(
                    ean = "1234567890",
                    material = "000000000000123456",
                    name = "Товар весовой",
                    uom = Uom.DEFAULT
            )
        }
    }

}

interface ICheckListRepo {
    fun getGoodInfoByMaterial(material: String): GoodInfo?
}