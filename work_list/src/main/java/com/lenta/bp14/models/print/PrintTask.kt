package com.lenta.bp14.models.print

import com.lenta.bp14.fmp.resources.ZfmpUtz50V001
import com.lenta.bp14.fmp.resources.ZfmpUtz51V001
import com.lenta.bp14.models.check_price.ActualPriceInfo
import com.lenta.bp14.models.check_price.GoodOptions
import com.lenta.bp14.models.data.getGoodType
import com.lenta.bp14.models.print.PriceTagType.Companion.emptyPriceTag
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.bp14.requests.ProductInfoResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getMaxAllowedPrintCopyWkl
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.functional.Either
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.print.IPrintPriceNetService
import com.lenta.shared.print.PrintPriceInfo
import com.lenta.shared.print.PrintTemplate
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.toNullIfEmpty
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppScope
class PrintTask @Inject constructor(
        hyperHive: HyperHive,
        private val bigDatamaxPrint: BigDatamaxPrint,
        private val printPriceNetService: IPrintPriceNetService,
        private val timeMonitor: ITimeMonitor,
        private var repoInMemoryHolder: IRepoInMemoryHolder,
        private var sessionInfo: ISessionInfo
) : IPrintTask {

    override var matNrForPrint: String? = null

    private var maxPrintCopy: Int = 0

    private val emptyPrinterType = PrinterType(
            id = "", name = "Не выбрано", isMobile = null, isStatic = null
    )

    val zfmpUtz51V001 by lazy { ZfmpUtz51V001(hyperHive) }
    val zfmpUtz50V001 by lazy { ZfmpUtz50V001(hyperHive) }
    val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки

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

        // большой datamax
        if (printerType.id == "03") {
            return withContext(IO) {
                productInfoResult.productsInfo.getOrNull(0)?.let {
                    bigDatamaxPrint.printToBigDatamax(it.ean, isRegular, copies)
                } ?: Either.Left(Failure.ServerError)
            }

        }


        val serverPriceInfo = productInfoResult.prices.getOrNull(0)
                ?: return Either.Left(Failure.ServerError)


        productInfoResult.productsInfo.getOrNull(0)?.let { productInfo ->

            val actualPriceInfo = ActualPriceInfo(
                    matNumber = serverPriceInfo.matnr.takeLast(6),
                    productName = productInfo.name,
                    price1 = serverPriceInfo.price1,
                    price2 = serverPriceInfo.price2.toNullIfEmpty(),
                    price3 = serverPriceInfo.price3.toNullIfEmpty(),
                    price4 = serverPriceInfo.price4.toNullIfEmpty(),
                    options = GoodOptions(
                            matrixType = getMatrixType(productInfo.matrixType),
                            section = if (productInfo.sectionNumber.isNotEmpty()) productInfo.sectionNumber else "91",
                            goodType = getGoodType(
                                    alcohol = productInfo.isAlco,
                                    excise = productInfo.isExcise,
                                    marked = productInfo.isMarked),
                            healthFood = productInfo.isHealthyFood.isSapTrue(),
                            novelty = productInfo.isNew.isSapTrue()
                    )
            )

            val price2 = if (isRegular) actualPriceInfo.price2
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
                            address = repoInMemoryHolder.storesRequestResult?.markets?.firstOrNull { it.tkNumber == sessionInfo.market }?.address.orEmpty(),
                            promoBegin = serverPriceInfo.startPromo,
                            promoEnd = serverPriceInfo.endPromo,
                            copies = copies
                    )

            )
        }

        return Either.Left(Failure.ServerError)

    }

    override suspend fun printToBigDataMax(printTasks: List<PrintInfo>): Either<Failure, Boolean> {
        return bigDatamaxPrint.printToBigDatamax(printTasks)
    }

    override suspend fun loadMaxPrintCopy() {
        return withContext(IO) {
            maxPrintCopy = settings.getMaxAllowedPrintCopyWkl() ?: 0
        }
    }

    override fun getMaxCopies(): Int {
        return maxPrintCopy
    }

}

interface IPrintTask {

    var matNrForPrint: String?

    suspend fun getPriceTagTypes(): List<PriceTagType>
    suspend fun getPrinterTypes(): List<PrinterType>
    suspend fun printPrice(
            ip: String,
            productInfoResult: ProductInfoResult,
            printerType: PrinterType,
            isRegular: Boolean,
            copies: Int): Either<Failure, Boolean>

    suspend fun printToBigDataMax(
            printTasks: List<PrintInfo>
    ): Either<Failure, Boolean>

    suspend fun loadMaxPrintCopy()
    fun getMaxCopies(): Int
}

data class PriceTagType(
        val id: String,
        val name: String,
        /**
         * Признак – цена товара регулярная
         */
        val isRegular: Boolean?
) {
    companion object {
        val emptyPriceTag by lazy {
            PriceTagType(
                    id = "", name = "Не выбрано", isRegular = null
            )
        }

    }
}

data class PrinterType(
        val id: String,
        val name: String,
        /**
         * Признак - Мобильный принтер
         */
        val isMobile: Boolean?,
        /**
         * Признак - Стационарный принтер
         */
        val isStatic: Boolean?
)
