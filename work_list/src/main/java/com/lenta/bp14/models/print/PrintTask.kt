package com.lenta.bp14.models.print

import com.lenta.bp14.fmp.resources.ZfmpUtz50V001
import com.lenta.bp14.fmp.resources.ZfmpUtz51V001
import com.lenta.bp14.models.check_price.ActualPriceInfo
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.bp14.requests.ProductInfoResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.print.IPrintPriceNetService
import com.lenta.shared.print.PrintPriceInfo
import com.lenta.shared.print.PrintTemplate
import com.lenta.shared.utilities.extentions.isSapTrue
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppScope
class PrintTask @Inject constructor(
        hyperHive: HyperHive,
        private val printPriceNetService: IPrintPriceNetService,
        private val timeMonitor: ITimeMonitor,
        private var repoInMemoryHolder: IRepoInMemoryHolder,
        private var sessionInfo: ISessionInfo
) : IPrintTask {

    private val emptyPriceTag = PriceTagType(
            id = "", name = "Не выбранно", isRegular = false
    )

    private val emptyPrinterType = PrinterType(
            id = "", name = "Не выбранно", isMobile = false, isStatic = false
    )

    val zfmpUtz51V001 by lazy {
        ZfmpUtz51V001(hyperHive)
    }

    val zfmpUtz50V001 by lazy {
        ZfmpUtz50V001(hyperHive)
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

    override suspend fun getPrinterTypes(): List<PrinterType> {
        return withContext(IO) {
            return@withContext mutableListOf(emptyPrinterType).apply {
                @Suppress("INACCESSIBLE_TYPE")
                addAll(zfmpUtz50V001.localHelper_ET_PRINTER_TYPE.all.map {
                    PrinterType(
                            id = it.printerCode, name = it.printerName, isMobile = it.isMobile.isSapTrue(), isStatic = it.isStatic.isSapTrue()
                    )
                })
            }.toList()

        }
    }

    override suspend fun printPrice(
            ip: String,
            productInfoResult: ProductInfoResult,
            printerType: PrinterType,
            isRegular: Boolean,
            copies: Int): Either<Failure, Boolean> {

        val serverPriceInfo = productInfoResult.prices.getOrNull(0)
                ?: return Either.Left(Failure.ServerError)


        productInfoResult.productsInfo.getOrNull(0)?.let { productInfo ->

            val actualPriceInfo = ActualPriceInfo(
                    matNumber = serverPriceInfo.matnr.takeLast(6),
                    productName = productInfo.name,
                    price1 = serverPriceInfo.price1,
                    price2 = serverPriceInfo.price2,
                    price3 = serverPriceInfo.price3,
                    price4 = serverPriceInfo.price4
            )

            var price2 = if (isRegular) actualPriceInfo.price2
                    ?: actualPriceInfo.price1!! else actualPriceInfo.getDiscountCardPrice() ?: 0.0

            val printTemplate = when (printerType.id) {
                "01" -> if (isRegular) {
                    PrintTemplate.Zebra_Yellow_6_6
                } else {
                    PrintTemplate.Zebra_Red_6_6
                }
                "02" -> if (isRegular) {
                    PrintTemplate.Datamax_Yellow_6_6
                } else {
                    PrintTemplate.Datamax_Red_6_6
                }
                else -> null

            } ?: return Either.Left(Failure.PrintTemplateError)

            return printPriceNetService.print(
                    ip = ip,
                    printTemplate = printTemplate,
                    printPriceInfo = PrintPriceInfo(
                            goodsName = productInfo.name,
                            price1 = actualPriceInfo.getPrice() ?: 0.0,
                            price2 = price2,
                            productNumber = productInfo.matNr.takeLast(6),
                            ean = productInfo.ean,
                            date = timeMonitor.getServerDate(),
                            address = repoInMemoryHolder.storesRequestResult?.markets?.firstOrNull { it.number == sessionInfo.market }?.address
                                    ?: "",
                            promoBegin = "????",
                            promoEnd = "????",
                            copies = copies
                    )

            )
        }

        return Either.Left(Failure.ServerError)

    }


}

interface IPrintTask {

    suspend fun getPriceTagTypes(): List<PriceTagType>
    suspend fun getPrinterTypes(): List<PrinterType>
    suspend fun printPrice(
            ip: String,
            productInfoResult: ProductInfoResult,
            printerType: PrinterType,
            isRegular: Boolean,
            copies: Int): Either<Failure, Boolean>

}

data class PriceTagType(
        val id: String,
        val name: String,
        /**
         * Признак – цена товара регулярная
         */
        val isRegular: Boolean
)

data class PrinterType(
        val id: String,
        val name: String,
        /**
         * Признак - Мобильный принтер
         */
        val isMobile: Boolean,
        /**
         * Признак - Стационарный принтер
         */
        val isStatic: Boolean
)
