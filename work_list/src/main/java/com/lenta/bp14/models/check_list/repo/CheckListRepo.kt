package com.lenta.bp14.models.check_list.repo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_list.Good
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@AppScope
class CheckListRepo(
        hyperHive: HyperHive,
        val units: ZmpUtz07V001 = ZmpUtz07V001(hyperHive), // Единицы измерения
        //val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive), // Настройки
        //val stores: ZmpUtz23V001 = ZmpUtz23V001(hyperHive), // Список магазинов
        val productInfo: ZfmpUtz48V001 = ZfmpUtz48V001(hyperHive), // Информация о товаре
        val barCodeInfo: ZmpUtz25V001 = ZmpUtz25V001(hyperHive) // Информация о штрих-коде
) : ICheckListRepo {

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
        return null
    }

    override suspend fun getUnitsName(code: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code)
        }
    }

}

interface ICheckListRepo {
    fun getGoodByMaterial(material: String): Good?
    fun getGoodByEan(ean: String): Good?

    suspend fun getUnitsName(code: String?): String?
}