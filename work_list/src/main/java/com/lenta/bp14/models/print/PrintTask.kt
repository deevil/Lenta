package com.lenta.bp14.models.print

import com.lenta.shared.di.AppScope
import com.lenta.shared.utilities.extentions.isSapTrue
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppScope
class PrintTask @Inject constructor(hyperHive: HyperHive) : IPrintTask {

    private val emptyPriceTag = PriceTagType(
            id = "", name = "Не выбранно", isRegular = false
    )

    val zfmpUtz51V001 by lazy {
        com.lenta.bp14.fmp.resources.ZfmpUtz51V001(hyperHive)
    }

    override suspend fun getPriceTagTypes(): List<PriceTagType> {
        return withContext(IO) {
            return@withContext mutableListOf(emptyPriceTag).apply {
                @Suppress("INACCESSIBLE_TYPE")
                addAll(zfmpUtz51V001.localHelper_ET_PRICE_TYPE.all.map {
                    PriceTagType(
                            id = it.templateCode, name = it.templateName, isRegular = it.isRegular.isSapTrue()
                    )
                })
            }.toList()

        }
    }


}

interface IPrintTask {

    suspend fun getPriceTagTypes(): List<PriceTagType>

}

data class PriceTagType(
        val id: String,
        val name: String,
        /**
         * Признак – цена товара регулярная
         */
        val isRegular: Boolean
)
