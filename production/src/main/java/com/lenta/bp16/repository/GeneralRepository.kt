package com.lenta.bp16.repository

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Good
import com.lenta.shared.fmp.resources.dao_ext.getAllowedProAppVersion
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeneralRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IGeneralRepository {

    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedProAppVersion()
        }
    }

    override fun getCurrentGood(): MutableLiveData<Good> {
        return MutableLiveData(Good(
                material = "000000000000002365",
                name = "Большая рыба",
                units = Uom.KG,
                planned = 25.0
        ))
    }

}

interface IGeneralRepository {

    suspend fun getAllowedAppVersion(): String?

    fun getCurrentGood(): MutableLiveData<Good>

}