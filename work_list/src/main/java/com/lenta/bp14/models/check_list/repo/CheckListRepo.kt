package com.lenta.bp14.models.check_list.repo

import com.lenta.bp14.models.check_list.GoodInfo
import com.lenta.shared.models.core.Uom
import kotlin.random.Random

class CheckListRepo : ICheckListRepo {

    override fun getGoodInfoByMaterial(material: String): GoodInfo? {
        return when ((0..2).random()) {
            1 -> {
                GoodInfo(
                        ean = "11111111",
                        material = "000000000000999921",
                        name = "Штучный",
                        uom = Uom.DEFAULT
                )
            }
            2 -> {
                GoodInfo(
                        ean = "22222222",
                        material = "000000000000999921",
                        name = "Весовой",
                        uom = Uom.KG
                )
            }
            else -> {
                GoodInfo(
                        ean = (111111111111..999999999999).random().toString(),
                        material = "000000000000" + (111111..999999).random(),
                        name = "Товар",
                        uom = Uom.DEFAULT
                )
            }
        }
    }

    override fun getGoodInfoByEan(ean: String): GoodInfo? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGoodInfoByMatcode(matcode: String): GoodInfo? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

interface ICheckListRepo {
    fun getGoodInfoByMaterial(material: String): GoodInfo?
    fun getGoodInfoByEan(ean: String): GoodInfo?
    fun getGoodInfoByMatcode(matcode: String): GoodInfo?
}