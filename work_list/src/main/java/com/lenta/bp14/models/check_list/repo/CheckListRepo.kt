package com.lenta.bp14.models.check_list.repo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_list.Good
import com.lenta.shared.models.core.Uom
import kotlin.random.Random

class CheckListRepo : ICheckListRepo {

    override fun getGoodByMaterial(material: String): Good? {
        return when ((0..2).random()) {
            0 -> {
                if (Random.nextBoolean()) {
                    Good(
                            ean = "11111111",
                            material = "000000000000999921",
                            name = "Штучный",
                            quantity = MutableLiveData("1"),
                            units = Uom.ST
                    )
                } else {
                    Good(
                            ean = (111111111111..999999999999).random().toString(),
                            material = "000000000000" + (111111..999999).random(),
                            name = "Товар",
                            quantity = MutableLiveData("1"),
                            units = Uom.ST
                    )
                }
            }
            else -> {
                Good(
                        ean = "22222222",
                        material = "000000000000999921",
                        name = "Весовой",
                        quantity = MutableLiveData("0.6"),
                        units = Uom.KG
                )
            }
        }
    }

    override fun getGoodByEan(ean: String): Good? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGoodByMatcode(matcode: String): Good? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

interface ICheckListRepo {
    fun getGoodByMaterial(material: String): Good?
    fun getGoodByEan(ean: String): Good?
    fun getGoodByMatcode(matcode: String): Good?
}